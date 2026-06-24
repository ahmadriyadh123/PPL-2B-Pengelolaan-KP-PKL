package com.jtk.ps.api.unit;

import com.jtk.ps.api.dto.Token;
import com.jtk.ps.api.model.Account;
import com.jtk.ps.api.model.CustomUserDetails;
import com.jtk.ps.api.model.ERole;
import com.jtk.ps.api.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BUG-014 — Regression test untuk token expiration dan validasi konfigurasi JwtUtil.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String VALID_SECRET = "super-secure-secret-key-that-is-at-least-64-characters-long-and-random-1234567890";
    private static final Long ACCESS_EXPIRY_MS = 900000L; // 15 Menit
    private static final Long REFRESH_EXPIRY_MS = 604800000L; // 7 Hari

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", VALID_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", ACCESS_EXPIRY_MS);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", REFRESH_EXPIRY_MS);
    }

    @Test
    @DisplayName("validateJwtConfig() - konfigurasi valid tidak melempar exception")
    void validateJwtConfig_validConfig_noException() {
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(jwtUtil, "validateJwtConfig"));
    }

    @Test
    @DisplayName("validateJwtConfig() - JWT_SECRET kosong harus throw IllegalStateException")
    void validateJwtConfig_emptySecret_throwsException() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ReflectionTestUtils.invokeMethod(jwtUtil, "validateJwtConfig")
        );
        assertTrue(exception.getMessage().contains("JWT_SECRET tidak diset"));
    }

    @Test
    @DisplayName("validateJwtConfig() - JWT_SECRET terlalu pendek harus throw IllegalStateException")
    void validateJwtConfig_shortSecret_throwsException() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "short-secret");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ReflectionTestUtils.invokeMethod(jwtUtil, "validateJwtConfig")
        );
        assertTrue(exception.getMessage().contains("JWT_SECRET terlalu pendek"));
    }

    @Test
    @DisplayName("validateJwtConfig() - refresh expiry <= access expiry harus throw IllegalStateException")
    void validateJwtConfig_refreshLessThanAccess_throwsException() {
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", 500000L);
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 900000L);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ReflectionTestUtils.invokeMethod(jwtUtil, "validateJwtConfig")
        );
        assertTrue(exception.getMessage().contains("harus lebih besar dari"));
    }

    @Test
    @DisplayName("generateAccessToken() - expiry token harus ~15 menit (900000 ms)")
    void generateAccessToken_expiryTimeMatchesConfig() {
        Account account = new Account(1, "panitiad3", "1234", ERole.COMMITTEE);
        CustomUserDetails userDetails = new CustomUserDetails(account);

        Token token = jwtUtil.generateAccessToken(userDetails);

        assertNotNull(token);
        assertEquals(Token.TokenType.ACCESS, token.getTokenType());

        // Cek expiry date token
        LocalDateTime now = LocalDateTime.now();
        long diffMinutes = ChronoUnit.MINUTES.between(now, token.getExpiryDate());
        
        // Seharusnya bertambah 15 menit ( toleransi waktu eksekusi < 1 menit)
        assertTrue(diffMinutes >= 14 && diffMinutes <= 16, 
            "Masa aktif access token harus ~15 menit, didapat: " + diffMinutes + " menit");
    }
}
