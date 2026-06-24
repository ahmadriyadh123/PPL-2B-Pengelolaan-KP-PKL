package com.kppkl.authcommons.filter;

import com.kppkl.authcommons.reader.CookieTokenReader;
import com.kppkl.authcommons.validator.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * S3-T01 — Filter JWT stateless untuk service downstream.
 *
 * Dirancang berdasarkan pelajaran dari Sprint 2:
 * - TIDAK ada HTTP call ke /account/verify (validasi lokal)
 * - TIDAK ada SecurityContextLogoutHandler (S2-T08)
 * - TIDAK ada System.out.println — semua log via SLF4J (S2-T10)
 * - shouldNotFilter() membaca path publik dari konfigurasi (S2-T09 pattern)
 * - Null-safe di semua titik (S2-T02 pattern)
 *
 * Cara kerja:
 * 1. Baca token dari cookie menggunakan CookieTokenReader
 * 2. Validasi token secara lokal menggunakan JwtTokenValidator
 * 3. Ekstrak claims (sub, id_role, id_prodi) dan set ke SecurityContext
 * 4. Teruskan request ke handler berikutnya
 */
@Slf4j
public class StatelessJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CLAIM_ID_ROLE  = "id_role";
    private static final String CLAIM_ID_PRODI = "id_prodi";

    private final JwtTokenValidator jwtTokenValidator;
    private final CookieTokenReader cookieTokenReader;
    private final List<String> publicPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public StatelessJwtAuthenticationFilter(
            JwtTokenValidator jwtTokenValidator,
            CookieTokenReader cookieTokenReader,
            List<String> publicPaths) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.cookieTokenReader  = cookieTokenReader;
        this.publicPaths        = publicPaths != null ? publicPaths : Collections.emptyList();
    }

    /**
     * Path publik tidak perlu melewati filter ini sama sekali.
     * Daftar path dibaca dari konfigurasi (auth.public-paths).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        boolean isPublic = publicPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
        if (isPublic) {
            log.debug("[StatelessJwtFilter] Path publik dilewati: {}", requestPath);
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Baca token dari cookie (null-safe — return Optional)
        String token = cookieTokenReader.readAccessToken(request).orElse(null);

        if (token == null) {
            // Tidak ada token → teruskan; SecurityConfig yang memutuskan
            // apakah endpoint ini membutuhkan autentikasi atau tidak
            filterChain.doFilter(request, response);
            return;
        }

        // Validasi token secara lokal
        Claims claims = jwtTokenValidator.validate(token);

        if (claims == null) {
            // Token tidak valid → teruskan tanpa set authentication
            // Spring Security akan menolak request jika endpoint membutuhkan auth
            log.warn("[StatelessJwtFilter] Token tidak valid pada request ke: {}",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Token valid — ekstrak informasi dan set ke SecurityContext
        try {
            String subject  = claims.getSubject();          // account ID
            Integer idRole  = claims.get(CLAIM_ID_ROLE, Integer.class);
            String roleName = resolveRoleName(idRole);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            subject,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(roleName))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("[StatelessJwtFilter] Autentikasi berhasil: sub={}, role={}",
                    subject, roleName);

        } catch (Exception ex) {
            // Pertahanan terakhir — jangan crash, cukup log
            log.warn("[StatelessJwtFilter] Gagal membaca claims dari token yang valid: {}",
                    ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Mengubah id_role integer ke nama authority string.
     * Sesuai dengan konstanta di account-service.
     */
    private String resolveRoleName(Integer idRole) {
        if (idRole == null) return "UNKNOWN";
        switch (idRole) {
            case 0: return "COMMITTEE";
            case 1: return "PARTICIPANT";
            case 2: return "COMPANY";
            case 3: return "HEAD_STUDY_PROGRAM";
            default:
                log.warn("[StatelessJwtFilter] id_role tidak dikenal: {}", idRole);
                return "UNKNOWN";
        }
    }
}
