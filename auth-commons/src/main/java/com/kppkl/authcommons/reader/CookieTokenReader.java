package com.kppkl.authcommons.reader;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * S3-T01 — Membaca token JWT dari cookie HTTP request.
 *
 * Tidak melakukan parsing Cookie header secara manual —
 * menggunakan HttpServletRequest.getCookies() yang sudah
 * di-handle oleh Servlet container.
 *
 * Return type Optional<String> untuk null-safety
 * (lanjutan S2-T02 dari JwtUtil.getUsernameFromToken).
 */
@Slf4j
public class CookieTokenReader {

    private final String accessTokenCookieName;

    public CookieTokenReader(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
    }

    /**
     * Membaca nilai access token dari cookie request.
     *
     * @param request HTTP request yang masuk
     * @return Optional berisi token string jika cookie ditemukan,
     *         Optional.empty() jika tidak ada atau cookie null
     */
    public Optional<String> readAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value == null || value.isBlank()) {
                    log.warn("[CookieTokenReader] Cookie '{}' ditemukan tapi nilainya kosong",
                            accessTokenCookieName);
                    return Optional.empty();
                }
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
