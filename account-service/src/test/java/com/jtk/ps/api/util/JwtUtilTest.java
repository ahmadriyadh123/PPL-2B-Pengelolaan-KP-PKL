package com.jtk.ps.api.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.jtk.ps.api.dto.Token;
import com.jtk.ps.api.model.Account;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import com.jtk.ps.api.repository.AccountRepository;
import com.jtk.ps.api.repository.LecturerRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    private static final String VALID_SECRET =
        "ini-secret-minimal-64-karakter-untuk-hs512-supaya-aman-dan-lulus-test";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", VALID_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 900_000L);    // 15 menit
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", 604_800_000L); // 7 hari
    }

    // UT-SEC-01: Access token expiry = 15 menit (900 detik)
    @Test
    void accessToken_shouldExpireIn15Minutes() {
        long before = System.currentTimeMillis();
        Token token = invokeDoGenerateToken(new HashMap<>(Map.of("id_role", 1)), "user123");
        long after = System.currentTimeMillis();

        Claims claims = Jwts.parser().setSigningKey(VALID_SECRET)
            .parseClaimsJws(token.getTokenValue()).getBody();

        long diffSec = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertEquals(900L, diffSec, 5); // toleransi 5 detik
    }

    // UT-SEC-02: Refresh token expiry = 7 hari (604800 detik)
    @Test
    void refreshToken_shouldExpireIn7Days() {
        Token token = jwtUtil.generateRefreshToken("user123");

        Claims claims = Jwts.parser().setSigningKey(VALID_SECRET)
            .parseClaimsJws(token.getTokenValue()).getBody();

        long diffSec = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertEquals(604800L, diffSec, 5);
        assertTrue(diffSec > 900);
    }

    // UT-SEC-03: Fail-fast jika JWT_SECRET tidak di-set
    @Test
    void validateJwtConfig_shouldThrow_whenSecretEmpty() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "");
        assertThrows(IllegalStateException.class, () -> jwtUtil.validateJwtConfig());
    }

    // UT-SEC-04: getUsernameFromToken mengembalikan username dengan benar
    @Test
    void getUsernameFromToken_shouldReturnUsername_whenTokenValid() {
        when(accountRepository.findById(123)).thenReturn(
            Optional.of(buildAccount(123, "mahasiswa01"))
        );
        String token = buildRawToken(new HashMap<>(Map.of("id_role", 1)), "123", 900_000L);
        Optional<String> result = jwtUtil.getUsernameFromToken(token);
        assertTrue(result.isPresent());
        assertEquals("mahasiswa01", result.get());
    }

    // UT-SEC-05: getUsernameFromToken aman saat token null/blank
    @Test
    void getUsernameFromToken_shouldReturnEmpty_whenTokenNull() {
        assertDoesNotThrow(() -> {
            Optional<String> result = jwtUtil.getUsernameFromToken(null);
            assertTrue(result.isEmpty());
        });
    }

    // UT-SEC-06: validateToken false untuk token dengan signature dimanipulasi
    @Test
    void validateToken_shouldThrowBadCredentials_whenSignatureManipulated() {
        String validToken = buildRawToken(new HashMap<>(), "user1", 900_000L);
        String[] parts = validToken.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignatureXXX";

        assertThrows(BadCredentialsException.class, () -> jwtUtil.validateToken(tampered));
    }

    // UT-SEC-07: validateToken false untuk token expired
    @Test
    void validateToken_shouldThrowBadCredentials_whenTokenExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 1L);
        Token token = invokeDoGenerateToken(new HashMap<>(), "user1");
        Thread.sleep(10);
        assertThrows(Exception.class, () -> jwtUtil.validateToken(token.getTokenValue()));
    }

    private Token invokeDoGenerateToken(Map<String, Object> claims, String subject) {
        try {
            var method = JwtUtil.class.getDeclaredMethod("doGenerateToken", Map.class, String.class);
            method.setAccessible(true);
            return (Token) method.invoke(jwtUtil, claims, subject);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private String buildRawToken(Map<String, Object> claims, String subject, long expiryMs) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(SignatureAlgorithm.HS512, VALID_SECRET).compact();
    }

    private Account buildAccount(int id, String username) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        return account;
    }
}