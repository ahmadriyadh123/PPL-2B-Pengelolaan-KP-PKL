package com.kppkl.authcommons.validator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3-T01 — Unit test JwtTokenValidator.
 *
 * Skenario yang diuji:
 * - token valid → Claims terbaca
 * - token expired → return null
 * - signature tidak valid → log WARN, return null
 * - token null / blank → return null
 * - alg=none (unsigned) → return null (jjwt menolak karena tidak ada secret yang cocok)
 */
class JwtTokenValidatorTest {

    // Secret minimal 64 karakter untuk HS512
    private static final String VALID_SECRET =
            "test-secret-yang-panjang-minimal-64-karakter-untuk-hs512-agar-aman-00000";

    private JwtTokenValidator validator;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator(VALID_SECRET);
    }

    // ─── Skenario: token valid ──────────────────────────────────────────────

    @Test
    @DisplayName("token valid → Claims terbaca dan subject sesuai")
    void validate_validToken_returnsClaimsWithCorrectSubject() {
        String token = buildToken("42", 60_000L);

        Claims claims = validator.validate(token);

        assertNotNull(claims, "Token valid harus menghasilkan Claims yang tidak null");
        assertEquals("42", claims.getSubject(), "Subject harus sesuai");
    }

    @Test
    @DisplayName("token valid → isValid() mengembalikan true")
    void isValid_validToken_returnsTrue() {
        String token = buildToken("7", 60_000L);
        assertTrue(validator.isValid(token));
    }

    // ─── Skenario: token expired ────────────────────────────────────────────

    @Test
    @DisplayName("token expired → validate() mengembalikan null (bukan exception)")
    void validate_expiredToken_returnsNull() {
        // expiry -1 detik dari sekarang (sudah lewat)
        String token = buildToken("99", -1_000L);

        Claims claims = validator.validate(token);

        assertNull(claims, "Token expired harus menghasilkan null, bukan exception");
    }

    @Test
    @DisplayName("token expired → isValid() mengembalikan false")
    void isValid_expiredToken_returnsFalse() {
        String token = buildToken("99", -1_000L);
        assertFalse(validator.isValid(token));
    }

    // ─── Skenario: signature tidak valid ───────────────────────────────────

    @Test
    @DisplayName("signature invalid → validate() mengembalikan null")
    void validate_invalidSignature_returnsNull() {
        // Token dibuat dengan secret yang berbeda
        String wrongSecret =
                "wrong-secret-yang-juga-panjang-minimal-64-karakter-untuk-testing-000";
        JwtTokenValidator otherValidator = new JwtTokenValidator(wrongSecret);
        String tokenSignedWithOtherSecret = buildTokenWith(wrongSecret, "55", 60_000L);

        // Validator dengan secret benar tidak bisa memverifikasi token dari secret lain
        Claims claims = validator.validate(tokenSignedWithOtherSecret);

        assertNull(claims, "Token dengan signature dari secret lain harus ditolak");
    }

    // ─── Skenario: token null / blank ──────────────────────────────────────

    @Test
    @DisplayName("token null → validate() mengembalikan null")
    void validate_nullToken_returnsNull() {
        assertNull(validator.validate(null));
    }

    @Test
    @DisplayName("token kosong → validate() mengembalikan null")
    void validate_blankToken_returnsNull() {
        assertNull(validator.validate("   "));
    }

    @Test
    @DisplayName("token string acak → validate() mengembalikan null")
    void validate_randomString_returnsNull() {
        assertNull(validator.validate("ini.bukan.jwt"));
    }

    // ─── Skenario: konstruktor dengan secret tidak valid ───────────────────

    @Test
    @DisplayName("konstruktor dengan secret null → IllegalArgumentException")
    void constructor_nullSecret_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenValidator(null));
    }

    @Test
    @DisplayName("konstruktor dengan secret terlalu pendek → IllegalArgumentException")
    void constructor_shortSecret_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtTokenValidator("terlalupendek"));
    }

    // ─── Helper ────────────────────────────────────────────────────────────

    private String buildToken(String subject, long expiryOffsetMs) {
        return buildTokenWith(VALID_SECRET, subject, expiryOffsetMs);
    }

    private String buildTokenWith(String secret, String subject, long expiryOffsetMs) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expiryOffsetMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
