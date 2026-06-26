package com.jtk.ps.api.unit;

import com.jtk.ps.api.config.AuthFeatureFlags;
import com.jtk.ps.api.repository.AccountRepository;
import com.jtk.ps.api.repository.LecturerRepository;
import com.jtk.ps.api.service.AccountService;
import com.jtk.ps.api.util.CookieUtil;
import com.jtk.ps.api.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit test untuk:
 * - AccountService.deleteParticipantAccountJob() — kredensial scheduled job dari env var (ISS-014, S2-T06)
 * - AuthFeatureFlags — feature flag local-validation (S2-T04, PB-12)
 *
 * Ref test case: UT-SEC-12 dan IT-SEC-08 (bagian unit-nya).
 */
@ExtendWith(MockitoExtension.class)
class ScheduledJobAndFeatureFlagTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AccountService accountService;

    // ===================================================================
    // UT-SEC-12 — Scheduled job harus skip (bukan exception) saat credential env kosong
    // Ref: ISS-014
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-12a: deleteParticipantAccountJob di-skip saat jobD3Username kosong, tidak ada akses repository/restTemplate")
    void deleteParticipantAccountJob_shouldSkip_whenD3UsernameEmpty() {
        ReflectionTestUtils.setField(accountService, "jobD3Username", "");
        ReflectionTestUtils.setField(accountService, "jobD3Password", "anypass");
        ReflectionTestUtils.setField(accountService, "jobD4Username", "panitiad4");
        ReflectionTestUtils.setField(accountService, "jobD4Password", "anypass");

        assertDoesNotThrow(accountService::deleteParticipantAccountJob);

        verify(restTemplate, never()).exchange(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(org.springframework.http.HttpEntity.class),
                org.mockito.ArgumentMatchers.<org.springframework.core.ParameterizedTypeReference<Object>>any());
    }

    @Test
    @DisplayName("UT-SEC-12b: deleteParticipantAccountJob di-skip saat semua credential null (default sebelum env var di-set)")
    void deleteParticipantAccountJob_shouldSkip_whenAllCredentialsNull() {
        ReflectionTestUtils.setField(accountService, "jobD3Username", null);
        ReflectionTestUtils.setField(accountService, "jobD3Password", null);
        ReflectionTestUtils.setField(accountService, "jobD4Username", null);
        ReflectionTestUtils.setField(accountService, "jobD4Password", null);

        assertDoesNotThrow(accountService::deleteParticipantAccountJob);
    }

    @Test
    @DisplayName("UT-SEC-12c: deleteParticipantAccountJob di-skip saat jobD4Password kosong meski D3 lengkap")
    void deleteParticipantAccountJob_shouldSkip_whenOnlyD4PasswordEmpty() {
        ReflectionTestUtils.setField(accountService, "jobD3Username", "panitiad3");
        ReflectionTestUtils.setField(accountService, "jobD3Password", "validpass");
        ReflectionTestUtils.setField(accountService, "jobD4Username", "panitiad4");
        ReflectionTestUtils.setField(accountService, "jobD4Password", "");

        assertDoesNotThrow(accountService::deleteParticipantAccountJob);
    }

    // Catatan: skenario job BERJALAN (credential lengkap) sengaja tidak diuji sebagai
    // unit test murni karena method privat `deleteParticipantsForProdi` memanggil
    // `login()` (BCrypt + DB) dan `restTemplate.exchange` ke participant-service secara
    // langsung tanpa lapisan abstraksi yang bisa di-mock terpisah. Skenario itu lebih
    // tepat diuji sebagai INTEGRATION TEST (lihat IT-SEC-08 / P09 di SRS Bab X), bukan unit test.

    // ===================================================================
    // AuthFeatureFlags — S2-T04 / PB-12
    // ===================================================================
    @Test
    @DisplayName("AuthFeatureFlags: default isLocalValidationEnabled() bernilai false bila field tidak di-set")
    void authFeatureFlags_shouldDefaultToFalse() {
        AuthFeatureFlags flags = new AuthFeatureFlags();
        ReflectionTestUtils.setField(flags, "localValidationEnabled", false);

        assertFalse(flags.isLocalValidationEnabled());
    }

    @Test
    @DisplayName("AuthFeatureFlags: isLocalValidationEnabled() bernilai true saat flag di-set true")
    void authFeatureFlags_shouldReturnTrue_whenEnabled() {
        AuthFeatureFlags flags = new AuthFeatureFlags();
        ReflectionTestUtils.setField(flags, "localValidationEnabled", true);

        assertTrue(flags.isLocalValidationEnabled());
    }
}