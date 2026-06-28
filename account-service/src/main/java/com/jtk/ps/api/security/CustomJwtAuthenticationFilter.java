package com.jtk.ps.api.security;

import com.kppkl.authcommons.validator.JwtTokenValidator;
import com.jtk.ps.api.dto.RefreshResponse;
import com.jtk.ps.api.model.Account;
import com.jtk.ps.api.service.AccountService;
import com.jtk.ps.api.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * S3-T02 — Dual-mode: local validation paralel dengan HTTP call ke /account/verify.
 *
 * Dua mode operasi dikontrol oleh feature flag:
 *
 * MODE 1 (flag=false, DEFAULT):
 *   Perilaku lama — validasi dilakukan sepenuhnya oleh JwtUtil (account-service internal).
 *   Tidak ada perubahan fungsional bagi pengguna.
 *
 * MODE 2 (flag=true):
 *   Validasi lokal via JwtTokenValidator (dari auth-commons S3-T01) dijalankan PARALEL
 *   dengan path lama. Hasil dibandingkan: jika ada perbedaan pada sub atau id_role,
 *   log WARN dikeluarkan agar bisa dimonitor sebelum HTTP call dihapus (S3-T08).
 *   Autentikasi menggunakan hasil LOKAL saat mode ini aktif.
 *
 * Perbaikan dari Sprint 2 yang dipertahankan:
 * - TIDAK ada SecurityContextLogoutHandler (S2-T08)
 * - TIDAK ada System.out.println — semua log via @Slf4j (S2-T10)
 * - Null-safe via Optional (S2-T02)
 * - Duplikasi @Autowired JwtUtil dihapus (bad practice Sprint 2)
 */
@Slf4j
@Component
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.accessTokenCookieName}")
    private String accessTokenCookieName;

    @Value("${jwt.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    /**
     * Feature flag untuk mengaktifkan dual-mode validation (S3-T02).
     * Default false — path lama tetap aktif sampai flag diset via env var.
     * Set AUTH_LOCAL_VALIDATION_ENABLED=true untuk mengaktifkan.
     */
    @Value("${auth.local-validation.enabled:false}")
    private boolean authLocalValidationEnabled;

    // Satu injeksi saja — bad practice duplikasi dari Sprint 2 sudah diperbaiki
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountService accountService;

    /**
     * JwtTokenValidator dari auth-commons (S3-T01).
     * Digunakan hanya saat authLocalValidationEnabled=true.
     * Tidak wajib ada (service lain mungkin tidak punya bean ini).
     */
    @Autowired(required = false)
    private JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = getJwtFromCookie(request);

        if (!StringUtils.hasText(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (authLocalValidationEnabled && jwtTokenValidator != null) {
                // ── MODE 2: Dual-mode (S3-T02) ────────────────────────────────────
                runDualMode(jwtToken, request, response, filterChain);
            } else {
                // ── MODE 1: Path lama (default) ───────────────────────────────────
                runLegacyMode(jwtToken, request, response, filterChain);
            }
        } catch (ExpiredJwtException ex) {
            handleExpiredToken(ex, request, response);
        } catch (BadCredentialsException ex) {
            request.setAttribute("exception", ex);
        } catch (Exception ex) {
            log.warn("Token validation failed: {}", ex.getMessage());
            request.setAttribute("exception", ex);
        }

        filterChain.doFilter(request, response);
    }

    // ─── Mode 2: Dual validation (lokal + path lama, hasil dibandingkan) ───────

    /**
     * Menjalankan validasi lokal dan path lama secara paralel lalu membandingkan hasilnya.
     * Jika ada mismatch pada sub atau id_role, log WARN dikeluarkan.
     * Autentikasi menggunakan hasil LOKAL.
     */
    private void runDualMode(String jwtToken,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              FilterChain filterChain) throws IOException, ServletException {

        // Path lokal (dari auth-commons S3-T01)
        Claims localClaims = jwtTokenValidator.validate(jwtToken);

        // Path lama (validasi internal account-service)
        boolean remoteValid = false;
        String remoteSub = null;
        Integer remoteIdRole = null;
        try {
            remoteValid = jwtUtil.validateToken(jwtToken);
            if (remoteValid) {
                remoteSub    = jwtUtil.getIdAccountFromToken(jwtToken);
                remoteIdRole = jwtUtil.getRoleFromToken(jwtToken);
            }
        } catch (Exception ex) {
            log.warn("[DualMode] Path lama gagal validasi: {}", ex.getMessage());
        }

        // Bandingkan hasil
        if (localClaims != null && remoteValid) {
            String localSub    = localClaims.getSubject();
            Integer localIdRole = localClaims.get("id_role", Integer.class);

            boolean subMatch  = Objects.equals(localSub, remoteSub);
            boolean roleMatch = Objects.equals(localIdRole, remoteIdRole);

            if (!subMatch || !roleMatch) {
                log.warn("[S3-T02] LOCAL vs REMOTE mismatch — " +
                         "local.sub={} remote.sub={} | local.id_role={} remote.id_role={} | timestamp={}",
                        localSub, remoteSub, localIdRole, remoteIdRole,
                        Instant.now());
                // JANGAN log raw token — hanya metadata klaim
            }
        } else if (localClaims == null && remoteValid) {
            log.warn("[S3-T02] LOCAL validasi GAGAL tapi REMOTE berhasil — " +
                     "remote.sub={} remote.id_role={} | timestamp={}",
                    remoteSub, remoteIdRole, Instant.now());
        } else if (localClaims != null) {
            log.warn("[S3-T02] LOCAL validasi berhasil tapi REMOTE GAGAL — " +
                     "local.sub={} local.id_role={} | timestamp={}",
                    localClaims.getSubject(), localClaims.get("id_role"), Instant.now());
        }

        // Autentikasi menggunakan hasil LOKAL saat dual-mode aktif
        if (localClaims != null) {
            authenticateFromLocalClaims(localClaims, request);
        }
    }

    // ─── Mode 1: Path lama (default) ────────────────────────────────────────────

    private void runLegacyMode(String jwtToken,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) {
        if (jwtUtil.validateToken(jwtToken)) {
            jwtUtil.getUsernameFromToken(jwtToken).ifPresent(username -> {
                UserDetails userDetails = accountService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }
    }

    // ─── Set autentikasi dari Claims lokal ──────────────────────────────────────

    /**
     * Set SecurityContext dari Claims yang sudah divalidasi secara lokal.
     * Menggunakan subject (account ID integer) untuk lookup username ke database
     * melalui AccountService, sehingga tidak perlu token mentah lagi.
     */
    private void authenticateFromLocalClaims(Claims claims, HttpServletRequest request) {
        try {
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                log.warn("[DualMode] Claims subject kosong, skip autentikasi");
                return;
            }
            int accountId = Integer.parseInt(subject);
            Account account = accountService.findAccountById(accountId);
            if (account == null) {
                log.warn("[DualMode] Account dengan id={} tidak ditemukan", accountId);
                return;
            }
            UserDetails userDetails = accountService.loadUserByUsername(account.getUsername());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (NumberFormatException ex) {
            log.warn("[DualMode] Subject bukan angka valid: {}", ex.getMessage());
        } catch (Exception ex) {
            log.warn("[DualMode] Gagal set autentikasi dari local claims: {}", ex.getMessage());
        }
    }

    // ─── Expired token — coba refresh ───────────────────────────────────────────

    private void handleExpiredToken(ExpiredJwtException ex,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        try {
            String refreshToken = getRefreshToken(request);
            if (StringUtils.hasText(refreshToken) && jwtUtil.validateToken(refreshToken)) {
                allowForRefreshToken(ex, request, response);
            } else {
                request.setAttribute("exception", ex);
            }
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
    }

    private void allowForRefreshToken(ExpiredJwtException ex,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        RefreshResponse refreshResponse = accountService.refresh(getRefreshToken(request));
        Map<String, Object> r = refreshResponse.getResponse();
        String sub = (String) r.get("sub");
        Account account = accountService.findAccountById(Integer.parseInt(sub));
        if (account != null) {
            UserDetails userDetails = accountService.loadUserByUsername(account.getUsername());
            refreshResponse.getHeaders().forEach((key, value) -> {
                if (value.get(0) != null) {
                    response.setHeader(key, value.get(0));
                    HttpCookie.parse(value.get(0)).forEach(cookie -> {
                        request.setAttribute("accessToken", cookie.getValue());
                        response.addCookie(new Cookie(cookie.getName(), cookie.getValue()));
                    });
                }
            });
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    // ─── Helper: baca cookie ─────────────────────────────────────────────────────

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (refreshTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
