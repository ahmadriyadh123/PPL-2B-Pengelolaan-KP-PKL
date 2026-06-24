package com.jtk.ps.api.unit;

import com.jtk.ps.api.model.Utility;
import com.jtk.ps.api.repository.CriteriaMappingRepository;
import com.jtk.ps.api.repository.FinalMappingRepository;
import com.jtk.ps.api.repository.ParticipantRankingRepository;
import com.jtk.ps.api.repository.UtilityDateRepository;
import com.jtk.ps.api.repository.UtilityRepository;
import com.jtk.ps.api.service.MappingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test — MappingService (White Box)
 * Professional Execution Logging Edition (Dynamic Status Detection)
 */
@ExtendWith(MockitoExtension.class)
class MappingServiceTest {

    @Mock
    private UtilityRepository utilityRepository;

    @Mock
    private FinalMappingRepository finalMappingRepository;

    @Mock
    private CriteriaMappingRepository criteriaMappingRepository;

    @Mock
    private ParticipantRankingRepository participantRankingRepository;

    @Mock
    private UtilityDateRepository utilityDateRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WebClient.Builder webClient;

    @InjectMocks
    private MappingService mappingService;

    // Tracker internal untuk mendeteksi status kegagalan
    private static boolean isCurrentTestPassed = true;

    // ── Professional Real-time Test Watcher ──────────────────────────────────
    @RegisterExtension
    static final TestWatcher watcher = new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            isCurrentTestPassed = false;
        }
        @Override
        public void testSuccessful(ExtensionContext context) {
            isCurrentTestPassed = true;
        }
    };

    @AfterEach
    void cetakLogPerModulProfessional(TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();
        String[] parts = displayName.split("\\|");
        
        String unitTestId = parts[0].trim();
        String description = parts.length > 1 ? parts[1].trim() : displayName;
        String finalStatus = isCurrentTestPassed ? "[ PASSED ]" : "[ FAILED ]";

        System.out.println("\n");
        System.out.println("+------------------------------------------------------------------------------------+");
        System.out.printf("| TEST EXECUTION LOG - [%-74s] |\n", unitTestId);
        System.out.println("+------------------------------------------------------------------------------------+");
        System.out.printf("| MODULE      : %-68s |\n", description);
        System.out.printf("| STATUS      : %-66s |\n", finalStatus);
        System.out.println("| ISOLATION   : MOCKITO EXTENSION (CLEAN)                                            |");
        System.out.println("+------------------------------------------------------------------------------------+");
        System.out.println("  ..................................................................................  ");
        System.out.println("\n");
    }

    // ── Fixture Helper ─────────────────────────────────────────────────────────

    private Utility buildUtility(Integer id, Integer isFinal) {
        Utility u = new Utility();
        u.setId(id);
        u.setIsFinal(isFinal);
        return u;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUG-007 — Logika Validasi Get Final / Is Final Mapping
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("UT-004 | getFinalMapping() -> Negatif | Data Utility Tidak Ada di DB")
    void getFinalMapping_whenUtilityNotFound_shouldReturnZeroWithoutIndexOutOfBoundsException() {
        // 1. Mock findById return empty
        Integer nonExistentId = 999;
        when(utilityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 2 & 3. Panggil getFinalMapping dengan parameter sesuai signature aslinya
        Integer result = assertDoesNotThrow(
                () -> mappingService.getFinalMapping(nonExistentId, 2026, "cookie"),
                "getFinalMapping() tidak boleh throw IndexOutOfBoundsException saat data tidak ditemukan"
        );

        // 4. Assert return value == 0 (default fallback)
        assertEquals(0, result, "Harus mengembalikan nilai default 0 sebagai fallback aman");
        verify(utilityRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("UT-005 | getFinalMapping() -> Positif | Data Utility Ditemukan di DB")
    void getFinalMapping_whenUtilityFound_shouldReturnCorrectIsFinalValue() {
        // 1. Mock findById return Utility dengan isFinal = 1
        Integer existingId = 1;
        Utility utility = buildUtility(existingId, 1);
        when(utilityRepository.findById(existingId)).thenReturn(Optional.of(utility));

        // 2. Panggil getFinalMapping sesuai 3 parameter asli
        Integer result = mappingService.getFinalMapping(existingId, 2026, "cookie");

        // 3. Assert return value == 1 sesuai data mock
        assertEquals(1, result, "Harus mengembalikan nilai isFinal yang sesuai dengan data DB");
        verify(utilityRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("UT-006 | getIsFinalMapping() -> Negatif | ID Tidak Terdaftar (Boundary 0)")
    void getIsFinalMapping_withIdZero_shouldReturnDefaultZeroWithoutCrash() {
        // 1. Mock findById(0) return empty
        Integer boundaryId = 0;
        when(utilityRepository.findById(boundaryId)).thenReturn(Optional.empty());

        // 2 & 3. Panggil getIsFinalMapping
        Integer result = assertDoesNotThrow(
                () -> mappingService.getIsFinalMapping(boundaryId),
                "Tidak boleh terjadi crash atau IndexOutOfBoundsException saat id tidak terdaftar"
        );

        // 4. Assert return value = 0 (memastikan pola lama ar.get(0) sudah hilang)
        assertEquals(0, result, "Return value harus bernilai 0 sebagai fallback default");
        verify(utilityRepository, times(1)).findById(boundaryId);
        verifyNoMoreInteractions(utilityRepository);
    }
}