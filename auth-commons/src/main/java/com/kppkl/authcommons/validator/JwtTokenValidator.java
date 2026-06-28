package com.kppkl.authcommons.validator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

/**
 * S3-T01 — Memvalidasi JWT token secara lokal tanpa HTTP call ke /account/verify.
 *
 * Ini adalah inti dari migrasi Sprint 3: setiap service downstream
 * memvalidasi token sendiri menggunakan shared secret,
 * bukan bergantung pada account-service.
 *
 * Prinsip yang diterapkan:
 * - Null-safe: semua kondisi token tidak valid dikembalikan null (lanjutan S2-T02)
 * - Log via SLF4J bukan System.out (lanjutan S2-T10)
 * - alg=none ditolak secara implisit karena token tanpa signature
 *   tidak akan cocok dengan secret key apapun di jjwt 0.9.1
 */
@Slf4j
public class JwtTokenValidator {

    private final String tokenSecret;

    public JwtTokenValidator(String tokenSecret) {
        if (tokenSecret == null || tokenSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "[JwtTokenValidator] tokenSecret tidak boleh kosong. " +
                    "Set JWT_SECRET di environment variable.");
        }
        if (tokenSecret.length() < 64) {
            throw new IllegalArgumentException(
                    "[JwtTokenValidator] tokenSecret terlalu pendek (" +
                    tokenSecret.length() + " karakter). Minimal 64 karakter.");
        }
        this.tokenSecret = tokenSecret;
    }

    /**
     * Memvalidasi dan mem-parse token JWT.
     *
     * @param token JWT string dari cookie
     * @return Claims jika token valid, null jika tidak valid atau expired
     */
    public Claims validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .setSigningKey(tokenSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            log.warn("[JwtTokenValidator] Token sudah expired: {}", ex.getMessage());
            return null;
        } catch (SignatureException ex) {
            log.warn("[JwtTokenValidator] Signature token tidak valid — " +
                     "kemungkinan token dimanipulasi: {}", ex.getMessage());
            return null;
        } catch (MalformedJwtException ex) {
            log.warn("[JwtTokenValidator] Format token tidak valid: {}", ex.getMessage());
            return null;
        } catch (UnsupportedJwtException ex) {
            log.warn("[JwtTokenValidator] Tipe token tidak didukung " +
                     "(kemungkinan alg=none): {}", ex.getMessage());
            return null;
        } catch (IllegalArgumentException ex) {
            log.warn("[JwtTokenValidator] Token kosong atau null: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Mengecek apakah token valid tanpa mengembalikan Claims.
     * Berguna untuk pengecekan cepat di middleware.
     *
     * @param token JWT string
     * @return true jika token valid dan belum expired
     */
    public boolean isValid(String token) {
        return validate(token) != null;
    }
}
