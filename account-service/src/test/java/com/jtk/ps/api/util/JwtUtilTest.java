package com.jtk.ps.api.util;

import com.jtk.ps.api.dto.Token;
import com.jtk.ps.api.model.Account;
import com.jtk.ps.api.model.CustomUserDetails;
import com.jtk.ps.api.model.ERole;
import com.jtk.ps.api.repository.AccountRepository;
import com.jtk.ps.api.repository.LecturerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Unit test untuk JwtUtil (account-service).
 *
 * Sengaja ditempatkan di package com.jtk.ps.api.util (bukan ...unit) supaya
 * bisa mengakses method package-private validateJwtConfig() tanpa reflection
 * tambahan — method tersebut adalah inti dari ISS-017 (validasi fail-fast
 * JWT_SECRET).
 *
 * Field @Value (tokenSecret, tokenExpirationMsec, refreshTokenExpirationMsec)
 * tidak otomatis terisi oleh @InjectMocks karena bukan dependency Spring bean,
 * sehingga di-set manual lewat ReflectionTestUtils pada setiap test/ @BeforeEach.
 *
 * Ref test case: UT-SEC-01 s/d UT-SEC-09 (lihat dokumen test case Tim Security Sprint 2).
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String VALID_SECRET =
            "a-very-long-random-test-secret-value-that-is-at-least-64-characters-long-1234567890";

    @BeforeEach
    void setUpDefaultValidConfig() {
        // Konfigurasi default valid; masing-masing test override field yang relevan saja.
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", VALID_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 900_000L);       // 15 menit
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", 604_800_000L); // 7 hari
    }

    // ===================================================================
    // UT-SEC-01 — validateJwtConfig(): JWT_SECRET tidak diset
    // Ref: ISS-017, BUG-001
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-01: validateJwtConfig harus throw saat tokenSecret null")
    void validateJwtConfig_shouldThrow_whenSecretNull() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("JWT_SECRET tidak diset"));
    }

    @Test
    @DisplayName("UT-SEC-01b: validateJwtConfig harus throw saat tokenSecret string kosong/blank")
    void validateJwtConfig_shouldThrow_whenSecretBlank() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "   ");

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("JWT_SECRET tidak diset"));
    }

    // ===================================================================
    // UT-SEC-02 — validateJwtConfig(): secret < 64 karakter
    // Ref: ISS-017
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-02: validateJwtConfig harus throw saat secret kurang dari 64 karakter")
    void validateJwtConfig_shouldThrow_whenSecretTooShort() {
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "short-secret-10ch");

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("terlalu pendek"));
    }

    // ===================================================================
    // UT-SEC-03 — validateJwtConfig(): secret masih nilai default lemah "token"
    // Ref: ISS-017, BUG-001, F-03
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-03: validateJwtConfig harus throw saat secret masih nilai default 'token'")
    void validateJwtConfig_shouldThrow_whenSecretIsDefaultWeakValue() {
        // Tetap harus >=64 karakter dulu supaya benar-benar menguji pengecekan "token",
        // bukan ketahan di pengecekan panjang. Tapi sesuai kode asli, perbandingan
        // dilakukan terhadap nilai literal "token" apa adanya — jadi kita uji persis itu,
        // walau secara teknis nilai ini juga akan gagal di pengecekan panjang lebih dulu.
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", "token");

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        // Catatan temuan: karena urutan validasi (panjang dicek sebelum nilai default),
        // pesan yang keluar untuk kasus ini adalah "terlalu pendek", bukan
        // "nilai default yang tidak aman". Assert berikut membuktikan urutan tersebut.
        assertTrue(ex.getMessage().contains("terlalu pendek"),
                "Ditemukan: pesan error untuk secret='token' adalah soal panjang, bukan soal nilai default — " +
                        "validasi nilai default secara praktis tidak pernah tercapai untuk secret pendek.");
    }

    @Test
    @DisplayName("UT-SEC-03b: validateJwtConfig harus throw saat secret 'token' di-pad agar >=64 char tapi tetap nilai default")
    void validateJwtConfig_shouldThrow_whenSecretIsDefaultWeakValue_evenIfLongEnough() {
        // Skenario realistis: seseorang mengisi JWT_SECRET=token lalu padding agar lolos
        // panjang minimum tanpa mengubah nilai dasarnya — kode sumber membandingkan
        // trim() == "token" secara case-insensitive, sehingga padding TIDAK akan tertangkap
        // oleh pengecekan ini (karena trim()-nya bukan lagi persis "token").
        // Test ini mendokumentasikan bahwa current implementation TIDAK mendeteksi varian ini.
        String paddedButStillWeak = "token" + "x".repeat(64);
        ReflectionTestUtils.setField(jwtUtil, "tokenSecret", paddedButStillWeak);

        assertDoesNotThrow(jwtUtil::validateJwtConfig,
                "Temuan: secret yang diawali kata 'token' tapi sudah >=64 karakter LOLOS validasi default-weak-value. " +
                        "Validasi hanya cek exact-match 'token', bukan pattern/predictability.");
    }

    // ===================================================================
    // UT-SEC-04 — validateJwtConfig(): expiry tidak valid / refresh <= access
    // Ref: ISS-017, ISS-NEW-09
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-04a: validateJwtConfig harus throw saat tokenExpirationMsec null/<=0")
    void validateJwtConfig_shouldThrow_whenAccessExpiryInvalid() {
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 0L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("JWT_ACCESS_EXP_MS"));
    }

    @Test
    @DisplayName("UT-SEC-04b: validateJwtConfig harus throw saat refreshTokenExpirationMsec null/<=0")
    void validateJwtConfig_shouldThrow_whenRefreshExpiryInvalid() {
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", -1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("JWT_REFRESH_EXP_MS"));
    }

    @Test
    @DisplayName("UT-SEC-04c: validateJwtConfig harus throw saat refresh expiry <= access expiry")
    void validateJwtConfig_shouldThrow_whenRefreshNotGreaterThanAccess() {
        ReflectionTestUtils.setField(jwtUtil, "tokenExpirationMsec", 900_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpirationMsec", 500_000L); // lebih kecil dari access

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::validateJwtConfig);
        assertTrue(ex.getMessage().contains("harus lebih besar dari"));
    }

    @Test
    @DisplayName("UT-SEC-04d (positive): validateJwtConfig tidak throw saat semua konfigurasi valid")
    void validateJwtConfig_shouldPass_whenConfigIsValid() {
        assertDoesNotThrow(jwtUtil::validateJwtConfig);
    }

    // ===================================================================
    // UT-SEC-05 — getUsernameFromToken(): null-safety untuk token null/blank
    // Ref: ISS-019, F-09
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-05a: getUsernameFromToken mengembalikan Optional.empty saat token null")
    void getUsernameFromToken_shouldReturnEmpty_whenTokenNull() {
        Optional<String> result = jwtUtil.getUsernameFromToken(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("UT-SEC-05b: getUsernameFromToken mengembalikan Optional.empty saat token blank")
    void getUsernameFromToken_shouldReturnEmpty_whenTokenBlank() {
        Optional<String> result = jwtUtil.getUsernameFromToken("   ");
        assertTrue(result.isEmpty());
    }

    // ===================================================================
    // UT-SEC-06 — getUsernameFromToken(): token malformed tidak boleh melempar exception ke caller
    // Ref: ISS-019
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-06: getUsernameFromToken mengembalikan Optional.empty saat token malformed, tidak exception")
    void getUsernameFromToken_shouldReturnEmpty_whenTokenMalformed() {
        Optional<String> result = assertDoesNotThrow(() -> jwtUtil.getUsernameFromToken("ini.bukan.jwt.valid"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("UT-SEC-06b: getUsernameFromToken mengembalikan Optional.empty saat subject token bukan angka (NumberFormatException)")
    void getUsernameFromToken_shouldReturnEmpty_whenSubjectIsNotNumeric() {
        // Generate token dengan subject non-numeric (di luar API generateAccessToken normal)
        // untuk memastikan NumberFormatException ikut ditangkap, bukan hanya JwtException.
        String tokenWithNonNumericSubject = Jwts.builder()
                .setSubject("bukan-angka")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512, VALID_SECRET)
                .compact();

        Optional<String> result = assertDoesNotThrow(() -> jwtUtil.getUsernameFromToken(tokenWithNonNumericSubject));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("UT-SEC-06c (positive): getUsernameFromToken mengembalikan username saat token valid dan account ditemukan")
    void getUsernameFromToken_shouldReturnUsername_whenTokenValidAndAccountFound() {
        Account account = new Account(1, "191511015", "hashed-pw", ERole.PARTICIPANT);
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        String token = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512, VALID_SECRET)
                .compact();

        Optional<String> result = jwtUtil.getUsernameFromToken(token);
        assertTrue(result.isPresent());
        assertEquals("191511015", result.get());
    }

    // ===================================================================
    // UT-SEC-07 — validateToken(): signature tidak valid harus BadCredentialsException
    // Ref: ISS-019
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-07a: validateToken melempar BadCredentialsException saat signature ditandatangani secret lain")
    void validateToken_shouldThrowBadCredentials_whenSignatureInvalid() {
        String tokenSignedWithDifferentSecret = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512,
                        "secret-lain-yang-juga-panjang-tapi-berbeda-dari-secret-aslinya-1234567890")
                .compact();

        assertThrows(BadCredentialsException.class,
                () -> jwtUtil.validateToken(tokenSignedWithDifferentSecret));
    }

    @Test
    @DisplayName("UT-SEC-07b: validateToken melempar BadCredentialsException saat token malformed")
    void validateToken_shouldThrowBadCredentials_whenMalformed() {
        assertThrows(BadCredentialsException.class, () -> jwtUtil.validateToken("token.rusak.tidak-valid"));
    }

    // ===================================================================
    // UT-SEC-08 — validateToken(): token null harus return false, tidak NPE
    // Ref: ISS-019, F-09
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-08: validateToken mengembalikan false saat token null, tidak NPE")
    void validateToken_shouldReturnFalse_whenTokenNull() {
        boolean result = assertDoesNotThrow(() -> jwtUtil.validateToken(null));
        assertFalse(result);
    }

    @Test
    @DisplayName("UT-SEC-08b (positive): validateToken mengembalikan true untuk token valid")
    void validateToken_shouldReturnTrue_whenTokenValid() {
        Token token = jwtUtil.generateRefreshToken("1");
        assertTrue(jwtUtil.validateToken(token.getTokenValue()));
    }

    // ===================================================================
    // UT-SEC-09 — doGenerateToken()/generateRefreshToken(): masih pakai API jjwt lama
    // Ref: ISS-025, S2-T01 (belum dikerjakan per tracking)
    // Test ini adalah round-trip behavioral check, BUKAN bukti definitif versi
    // library — tujuannya memastikan token yang diterbitkan dengan API
    // SignatureAlgorithm.HS512 + parser lama masih bisa di-generate & di-parse
    // ulang secara konsisten selama S2-T01 belum di-merge.
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-09: token hasil generateAccessToken bisa di-parse ulang dengan secret yang sama (legacy signing API)")
    void doGenerateToken_roundTrip_shouldBeParsableWithLegacyApi() {
        Account account = new Account(5, "kabayan", "hashed-pw", ERole.COMPANY);
        CustomUserDetails userDetails = new CustomUserDetails(account);

        Token accessToken = jwtUtil.generateAccessToken(userDetails);

        assertNotNull(accessToken.getTokenValue());
        assertEquals(Token.TokenType.ACCESS, accessToken.getTokenType());
        assertTrue(jwtUtil.validateToken(accessToken.getTokenValue()));

        Integer role = jwtUtil.getRoleFromToken(accessToken.getTokenValue());
        assertEquals(ERole.COMPANY.id, role);
    }

    @Test
    @DisplayName("UT-SEC-09b: generateAccessToken untuk role COMMITTEE menghasilkan claim id_role=0")
    void generateAccessToken_forCommittee_shouldHaveIdRoleZero() {
        Account account = new Account(2, "panitia-uji", "hashed-pw", ERole.COMMITTEE);
        CustomUserDetails userDetails = new CustomUserDetails(account);

        Token accessToken = jwtUtil.generateAccessToken(userDetails);
        ERole resolvedRole = jwtUtil.getRolesFromToken(accessToken.getTokenValue());

        assertEquals(ERole.COMMITTEE, resolvedRole);
    }

    // ===================================================================
    // Temuan tambahan (di luar tabel awal) — getRolesFromToken() berisiko NPE
    // saat claim id_role bernilai null karena unboxing langsung di switch(role).
    // Ditulis sebagai dokumentasi gap, bukan ekspektasi pass/fail mutlak.
    // ===================================================================
    @Test
    @DisplayName("TEMUAN: getRolesFromToken berpotensi NPE saat claim id_role tidak ada di token")
    void getRolesFromToken_currentBehavior_whenIdRoleClaimMissing() {
        String tokenWithoutIdRoleClaim = Jwts.builder()
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(SignatureAlgorithm.HS512, VALID_SECRET)
                .compact();

        // Didokumentasikan sebagai temuan: switch(role) pada kode asli melakukan
        // unboxing Integer->int, sehingga claim id_role yang hilang akan
        // menyebabkan NullPointerException, bukan default ke PARTICIPANT seperti
        // yang mungkin diharapkan dari blok `default:` pada switch tersebut.
        assertThrows(NullPointerException.class,
                () -> jwtUtil.getRolesFromToken(tokenWithoutIdRoleClaim),
                "Jika assertion ini gagal (tidak NPE), berarti behaviour sudah berubah — update dokumentasi temuan.");
    }
}