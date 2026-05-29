package com.jtk.ps.api.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${jwt.accessTokenCookieName}")
    private String accessTokenCookieName;

    @Value("${jwt.refreshTokenCookieName}")
    private String refreshTokenCookieName;

    public HttpCookie createAccessTokenCookie(String token, Long duration) {
        // [S2-T11] Tambah SameSite=Strict sesuai RFC 6265 bis untuk mitigasi CSRF.
        // .secure(true) diaktifkan saat deployment sudah pakai HTTPS.
        return ResponseCookie.from(accessTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                // .secure(true) // aktifkan di production (HTTPS)
                .build();
    }

    public HttpCookie createRefreshTokenCookie(String token, Long duration) {
        return ResponseCookie.from(refreshTokenCookieName, token)
                .maxAge(duration)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                // .secure(true) // aktifkan di production (HTTPS)
                .build();
    }

    public HttpCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(accessTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .build();
    }

    public HttpCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .build();
    }
}