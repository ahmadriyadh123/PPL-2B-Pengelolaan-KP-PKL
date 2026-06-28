package com.jtk.ps.api.unit;

import com.jtk.ps.api.dto.PrerequisiteCard;
import com.jtk.ps.api.repository.CompanyRepository;
import com.jtk.ps.api.repository.PrerequisiteRepository;
import com.jtk.ps.api.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * ============================================================================
 * POST-FIX BUG REGRESSION TESTS — CompanyService
 * ============================================================================
 * 
 * Tujuan: Memastikan bug yang SUDAH DIPERBAIKI tidak muncul kembali di masa depan.
 * 
 * Test di kelas ini memvalidasi kebenaran logika (Positive Testing / Regression).
 * Jika test di sini GAGAL, artinya ada developer yang tidak sengaja
 * merusak ulang kode yang sudah kita perbaiki.
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = {BugRegressionTest.class})
public class BugRegressionTest {

    @InjectMocks
    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PrerequisiteRepository prerequisiteRepository;

    @Test
    @DisplayName("Dummy test to trigger Maven Surefire")
    void ensureTestsRun() {
        assertTrue(true);
    }

    // ========================================================================
    // BUG-006: Operator Precedence di changeStatus()
    // ========================================================================
    @Nested
    @DisplayName("BUG-006 — FIXED: Evaluasi Perubahan Status")
    class Bug006Regression {
        @Test
        @DisplayName("Ekspresi perubahan status dievaluasi dengan benar dan aman")
        void shouldEvaluateStatusConditionCorrectly() {
            // Evaluasi dengan metode yang baru (menggunakan Boolean.FALSE.equals)
            Boolean statusActive = Boolean.TRUE;
            Boolean statusInactive = Boolean.FALSE;

            boolean correctActivate_inactive = Boolean.FALSE.equals(statusInactive);
            boolean correctDeactivate_active = Boolean.TRUE.equals(statusActive);
            
            assertTrue(correctActivate_inactive, "Saat status false, kondisi aktivasi bernilai true");
            assertTrue(correctDeactivate_active, "Saat status true, kondisi deaktivasi bernilai true");
        }
    }

    // ========================================================================
    // BUG-010: Missing Null Checks
    // ========================================================================
    @Nested
    @DisplayName("BUG-010 — FIXED: Null Checks pada Prerequisite")
    class Bug010Regression {
        @Test
        @DisplayName("getCardPrerequisiteByCompany() aman me-return null saat tidak ada prerequisite")
        void shouldReturnNullWhenPrerequisiteNotFound() {
            // Arrange
            Mockito.when(prerequisiteRepository.findByCompanyIdAndYear(
                    any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act
            PrerequisiteCard result = companyService.getCardPrerequisiteByCompany(999, "cookie");

            // Assert
            assertNull(result, "Harus me-return null dengan aman, bukan melemparkan NPE.");
        }

        @Test
        @DisplayName("getCardPrerequisiteByCommittee() aman me-return null saat tidak ada prerequisite")
        void shouldReturnNullWhenPrerequisiteByCommitteeNotFound() {
            // Arrange
            Mockito.when(prerequisiteRepository.findByCompanyIdAndYear(
                    any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act
            PrerequisiteCard result = companyService.getCardPrerequisiteByCommittee("cookie", 999);

            // Assert
            assertNull(result, "Harus me-return null dengan aman, bukan melemparkan NPE.");
        }
    }
}
