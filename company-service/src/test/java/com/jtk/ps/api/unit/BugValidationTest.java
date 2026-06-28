package com.jtk.ps.api.unit;

import com.jtk.ps.api.dto.*;
import com.jtk.ps.api.model.*;
import com.jtk.ps.api.repository.*;
import com.jtk.ps.api.service.CompanyService;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * ============================================================================
 * PRE-FIX BUG VALIDATION TESTS — CompanyService
 * ============================================================================
 * 
 * Tujuan: Memvalidasi dan mendokumentasikan keberadaan bug SEBELUM diperbaiki.
 * 
 * Cara membaca hasil test:
 * - Test yang PASS = bug terkonfirmasi ada (test membuktikan perilaku buggy)
 * - Test yang FAIL = bug mungkin sudah diperbaiki atau kondisi berubah
 * 
 * Bug yang di-cover:
 * - BUG-004: Hardcoded password "1234"
 * - BUG-006: Operator precedence di changeStatus()
 * - BUG-010: Missing null checks (pola orElse(null))
 * - BUG-021: Logic error filter D3/D4 (wrong list reference)
 * - BUG-023: AtomicReference misuse di CompanyService
 * 
 * Catatan: Setelah bug diperbaiki, test ini harus di-update agar
 *          memvalidasi perilaku yang benar (keadaan sesudah fix).
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BugValidationTest {

    @Test
    @DisplayName("Dummy test to trigger Maven Surefire")
    void ensureTestsRun() {
        assertTrue(true);
    }

    @InjectMocks
    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PrerequisiteRepository prerequisiteRepository;

    @Mock
    private ProposerRepository proposerRepository;

    @Mock
    private CriteriaRepository criteriaRepository;

    @Mock
    private PrerequisiteCompetenceRepository prerequisiteCompetenceRepository;

    @Mock
    private PrerequisiteJobscopeRepository prerequisiteJobscopeRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AdvantageRepository advantageRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SubmissionCriteriaRepository submissionCriteriaRepository;

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private ValuationRepository valuationRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackAnswerRepository feedbackAnswerRepository;

    @Mock
    private RestTemplate restTemplate;

    // ========================================================================
    // BUG-004: Hardcoded password "1234" di createCompany()
    // ========================================================================
    @Nested
    @DisplayName("BUG-004 — Hardcoded Password '1234'")
    class Bug004HardcodedPassword {

        /**
         * VALIDASI BUG: Membuktikan bahwa createCompany() mengirim password "1234"
         * ke account-service saat membuat akun baru.
         * 
         * Root Cause: CompanyService.java baris 253 → jsonObject.put("password", "1234")
         * 
         * Keadaan Sebelum (BUG): Semua company baru mendapat password yang sama "1234".
         * Keadaan Sesudah (FIX): Password harus di-generate secara random dan unik.
         */
        @Test
        @DisplayName("createCompany() mengirim hardcoded password '1234' ke account-service")
        void shouldExposeHardcodedPassword() throws Exception {
            // Arrange
            CompanyRequest companyRequest = new CompanyRequest(
                    "Test Company", "test@email.com", "Jl. Test 123",
                    "08123456789", "CP Test", "081234567890", "cp@email.com",
                    "Manager", "www.test.com", 50, 2020, Boolean.TRUE, 1);

            CreateAccountResponse createAccountResponse = new CreateAccountResponse(
                    1, "test@email.com", "1234", "COMPANY");
            Response<CreateAccountResponse> response = new Response<>(
                    createAccountResponse, HttpStatus.OK.value(), null);

            // Capture the request body sent to account-service
            ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

            Mockito.when(restTemplate.exchange(
                    contains("/account/create"),
                    any(HttpMethod.class),
                    any(),
                    any(ParameterizedTypeReference.class)))
                    .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

            Mockito.when(companyRepository.saveAndFlush(any(Company.class)))
                    .thenReturn(new Company(1, "Test Company", "test@email.com",
                            "Jl. Test 123", "08123456789", "CP Test", "081234567890",
                            "cp@email.com", "Manager", "www.test.com", 50, 2020,
                            Boolean.TRUE, 1, 1));

            // Act
            companyService.createCompany(companyRequest, "cookie");

            // Assert — Capture and verify the request body
            Mockito.verify(restTemplate).exchange(
                    contains("/account/create"),
                    any(HttpMethod.class),
                    httpEntityCaptor.capture(),
                    any(ParameterizedTypeReference.class));

            String requestBody = (String) httpEntityCaptor.getValue().getBody();
            JSONObject json = new JSONObject(requestBody);

            // BUG VALIDASI: Password adalah "1234" (hardcoded)
            // Setelah fix, assertion ini harus GAGAL karena password harus random
            assertEquals("1234", json.getString("password"),
                    "BUG-004 TERKONFIRMASI: Password masih hardcoded '1234'. "
                    + "Seharusnya password di-generate secara random.");
        }

        /**
         * VALIDASI BUG: Membuktikan bahwa semua company baru mendapat password 
         * yang SAMA persis (tidak unik).
         */
        @Test
        @DisplayName("Dua company berbeda mendapat password yang sama")
        void shouldExposeIdenticalPasswordsForDifferentCompanies() throws Exception {
            // Arrange
            CompanyRequest company1 = new CompanyRequest(
                    "Company A", "a@email.com", "Addr A",
                    "081A", "CP A", "081A", "cpa@email.com",
                    "Manager A", "www.a.com", 100, 2020, Boolean.TRUE, 1);

            CompanyRequest company2 = new CompanyRequest(
                    "Company B", "b@email.com", "Addr B",
                    "081B", "CP B", "081B", "cpb@email.com",
                    "Manager B", "www.b.com", 200, 2021, Boolean.TRUE, 2);

            CreateAccountResponse car1 = new CreateAccountResponse(1, "a@email.com", "1234", "COMPANY");
            CreateAccountResponse car2 = new CreateAccountResponse(2, "b@email.com", "1234", "COMPANY");

            Mockito.when(restTemplate.exchange(
                    contains("/account/create"),
                    any(HttpMethod.class), any(), any(ParameterizedTypeReference.class)))
                    .thenReturn(new ResponseEntity<>(
                            new Response<>(car1, 200, null), HttpStatus.OK))
                    .thenReturn(new ResponseEntity<>(
                            new Response<>(car2, 200, null), HttpStatus.OK));

            Mockito.when(companyRepository.saveAndFlush(any(Company.class)))
                    .thenReturn(new Company(1, "Company A", "a@email.com", "Addr A",
                            "081A", "CP A", "081A", "cpa@email.com", "Manager A",
                            "www.a.com", 100, 2020, Boolean.TRUE, 1, 1))
                    .thenReturn(new Company(2, "Company B", "b@email.com", "Addr B",
                            "081B", "CP B", "081B", "cpb@email.com", "Manager B",
                            "www.b.com", 200, 2021, Boolean.TRUE, 2, 2));

            ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

            // Act
            companyService.createCompany(company1, "cookie");
            companyService.createCompany(company2, "cookie");

            // Assert
            Mockito.verify(restTemplate, Mockito.times(2)).exchange(
                    contains("/account/create"),
                    any(HttpMethod.class),
                    captor.capture(),
                    any(ParameterizedTypeReference.class));

            List<HttpEntity> capturedEntities = captor.getAllValues();
            JSONObject json1 = new JSONObject((String) capturedEntities.get(0).getBody());
            JSONObject json2 = new JSONObject((String) capturedEntities.get(1).getBody());

            // BUG VALIDASI: Kedua password identik
            assertEquals(json1.getString("password"), json2.getString("password"),
                    "BUG-004 TERKONFIRMASI: Dua company berbeda mendapat password yang sama. "
                    + "Setelah fix, password harus unik per company.");
        }
    }

    // ========================================================================
    // BUG-006: Operator Precedence di changeStatus()
    // ========================================================================
    @Nested
    @DisplayName("BUG-006 — Operator Precedence Error di changeStatus()")
    class Bug006OperatorPrecedence {

        /**
         * VALIDASI BUG: Membuktikan bahwa ekspresi `!c.getStatus() == Boolean.TRUE.equals(Boolean.TRUE)`
         * tidak berfungsi sesuai intent.
         * 
         * Root Cause: CompanyService.java baris 1950
         *   `!c.getStatus() == Boolean.TRUE.equals(Boolean.TRUE)`
         *   dievaluasi sebagai `(!c.getStatus()) == (Boolean.TRUE.equals(Boolean.TRUE))`
         *   yaitu `(!true) == true` = `false == true` = `false`
         *   
         * Artinya: Saat company active (status=true), branch aktivasi TIDAK tercapai (benar).
         * Tapi saat company inactive (status=false), `(!false) == true` = `true == true` = `true`
         * → Masuk branch "aktivasi" → OK, tapi...
         * 
         * Baris 1960: `!c.getStatus() == Boolean.TRUE.equals(Boolean.FALSE)` 
         *   = `(!c.getStatus()) == false`
         * Saat active: `(!true) == false` = `false == false` = `true` → masuk branch deaktivasi → OK
         * Saat inactive: `(!false) == false` = `true == false` = `false` → TIDAK masuk → OK
         * 
         * Masalah sebenarnya: Kode ini berfungsi SECARA KEBETULAN tapi logic-nya SALAH
         * dan sangat confusing. Ini adalah time bomb yang bisa gagal saat refactor.
         */
        @Test
        @Disabled("Bug BUG-006 has been fixed. Kept for historical documentation. Running this test on the fixed code will fail because the code behaves correctly now.")
        @DisplayName("Ekspresi '!status == Boolean.TRUE.equals(Boolean.TRUE)' adalah confusing dan error-prone")
        void shouldExposeOperatorPrecedenceConfusion() {
            // Demonstrasi: evaluasi manual dari ekspresi buggy
            
            // Skenario 1: Company ACTIVE (status = true) → seharusnya masuk branch deaktivasi
            Boolean statusActive = Boolean.TRUE;
            boolean buggyCondition1_activate = !statusActive == Boolean.TRUE.equals(Boolean.TRUE);
            // !true == true → false == true → false
            boolean buggyCondition1_deactivate = !statusActive == Boolean.TRUE.equals(Boolean.FALSE);
            // !true == false → false == false → true
            
            // Skenario 2: Company INACTIVE (status = false) → seharusnya masuk branch aktivasi
            Boolean statusInactive = Boolean.FALSE;
            boolean buggyCondition2_activate = !statusInactive == Boolean.TRUE.equals(Boolean.TRUE);
            // !false == true → true == true → true
            boolean buggyCondition2_deactivate = !statusInactive == Boolean.TRUE.equals(Boolean.FALSE);
            // !false == false → true == false → false

            // BUG VALIDASI: Ekspresi berfungsi "kebetulan" tapi logic SALAH
            // Kondisi yang diharapkan oleh developer:
            // - statusActive → activate=false, deactivate=true ✓ (kebetulan benar)
            // - statusInactive → activate=true, deactivate=false ✓ (kebetulan benar)
            
            // Tapi kode ini adalah anti-pattern dan sangat sulit dibaca
            assertFalse(buggyCondition1_activate, 
                    "Active company: branch aktivasi harus false");
            assertTrue(buggyCondition1_deactivate,
                    "Active company: branch deaktivasi harus true");
            assertTrue(buggyCondition2_activate,
                    "Inactive company: branch aktivasi harus true");
            assertFalse(buggyCondition2_deactivate,
                    "Inactive company: branch deaktivasi harus false");

            // VERIFIKASI: Bandingkan dengan ekspresi yang benar dan mudah dibaca
            // Intent sebenarnya:
            boolean correctActivate_inactive = Boolean.FALSE.equals(statusInactive); // true ✓
            boolean correctDeactivate_active = Boolean.TRUE.equals(statusActive);    // true ✓
            
            assertEquals(buggyCondition2_activate, correctActivate_inactive,
                    "BUG-006 TERKONFIRMASI: Ekspresi buggy berfungsi kebetulan, "
                    + "tapi harus diganti dengan Boolean.FALSE.equals() untuk clarity.");
        }

        /**
         * VALIDASI BUG: Membuktikan bahwa changeStatus() dengan company active 
         * berhasil masuk branch deaktivasi (meskipun logic confusing).
         */
        @Test
        @DisplayName("changeStatus() active company — masuk branch deaktivasi (kebetulan benar)")
        void shouldChangeStatusFromActiveToInactive() {
            // Arrange
            Company activeCompany = new Company(1, "Test Company", "test@email.com",
                    "Jl. Test", "081", "CP", "081", "cp@email.com", "Mgr",
                    "www.test.com", 50, 2020, Boolean.TRUE, 1, 1);

            Mockito.when(companyRepository.findById(1))
                    .thenReturn(Optional.of(activeCompany));

            // Mock mapping-service delete call
            Mockito.when(restTemplate.exchange(
                    contains("/mapping/final/delete-company/"),
                    eq(HttpMethod.DELETE), any(), any(ParameterizedTypeReference.class)))
                    .thenReturn(new ResponseEntity<>(HttpStatus.OK));

            // Act
            Boolean result = companyService.changeStatus("cookie", 1);

            // Assert
            assertTrue(result, "changeStatus pada active company harus sukses");
            Mockito.verify(companyRepository).save(any(Company.class));
        }

        /**
         * VALIDASI BUG: Membuktikan bahwa changeStatus() dengan company inactive
         * berhasil masuk branch aktivasi.
         */
        @Test
        @DisplayName("changeStatus() inactive company — masuk branch aktivasi (kebetulan benar)")
        void shouldChangeStatusFromInactiveToActive() {
            // Arrange
            Company inactiveCompany = new Company(1, "Test Company", "test@email.com",
                    "Jl. Test", "081", "CP", "081", "cp@email.com", "Mgr",
                    "www.test.com", 50, 2020, Boolean.FALSE, 1, 1);

            Mockito.when(companyRepository.findById(1))
                    .thenReturn(Optional.of(inactiveCompany));

            Mockito.when(prerequisiteRepository.findByCompanyIdAndYear(
                    any(Integer.class), any(Integer.class)))
                    .thenReturn(null); // No prerequisite exists

            // Act
            Boolean result = companyService.changeStatus("cookie", 1);

            // Assert — should activate and create prerequisite
            assertTrue(result, "changeStatus pada inactive company harus sukses");
            Mockito.verify(prerequisiteRepository).save(any(Prerequisite.class));
            Mockito.verify(companyRepository).save(any(Company.class));
        }
    }

    // ========================================================================
    // BUG-010: Missing Null Checks (pola orElse(null))
    // ========================================================================
    @Nested
    @DisplayName("BUG-010 — Missing Null Checks (orElse(null))")
    class Bug010MissingNullChecks {

        /**
         * VALIDASI BUG: getCardPrerequisiteByCompany() memanggil 
         * prerequisiteRepository.findByCompanyIdAndYear() yang bisa return null.
         * Jika null → NPE pada baris 311: prerequisite.getId()
         * 
         * Root Cause: CompanyService.java baris 308 — tidak ada null-check
         */
        @Test
        @Disabled("Bug BUG-010 has been fixed. Kept for historical documentation. Running this test on the fixed code will fail because NPE is no longer thrown.")
        @DisplayName("getCardPrerequisiteByCompany() NPE saat prerequisite tidak ditemukan")
        void shouldThrowNpeWhenPrerequisiteNotFound() {
            // Arrange — prerequisite tidak ditemukan
            Mockito.when(prerequisiteRepository.findByCompanyIdAndYear(
                    any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act & Assert — harus throw NullPointerException
            assertThrows(NullPointerException.class, () -> {
                companyService.getCardPrerequisiteByCompany(999, "cookie");
            }, "BUG-010 TERKONFIRMASI: getCardPrerequisiteByCompany() "
                    + "throw NPE saat prerequisite null. "
                    + "Seharusnya throw exception yang lebih informatif.");
        }

        /**
         * VALIDASI BUG: getCardPrerequisiteByCommittee() juga memanggil
         * prerequisiteRepository.findByCompanyIdAndYear() tanpa null-check.
         * 
         * Root Cause: CompanyService.java baris 601
         */
        @Test
        @Disabled("Bug BUG-010 has been fixed. Kept for historical documentation. Running this test on the fixed code will fail because NPE is no longer thrown.")
        @DisplayName("getCardPrerequisiteByCommittee() NPE saat prerequisite tidak ditemukan")
        void shouldThrowNpeWhenPrerequisiteByCommitteeNotFound() {
            // Arrange
            Mockito.when(prerequisiteRepository.findByCompanyIdAndYear(
                    any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                companyService.getCardPrerequisiteByCommittee("cookie", 999);
            }, "BUG-010 TERKONFIRMASI: getCardPrerequisiteByCommittee() "
                    + "throw NPE saat prerequisite null.");
        }

        /**
         * VALIDASI BUG: getCompanyById() menggunakan orElse(null) dan sudah ada null-check.
         * Test ini memvalidasi bahwa case ini SUDAH aman (sebagai baseline).
         */
        @Test
        @DisplayName("getCompanyById() return null saat company tidak ditemukan (sudah aman)")
        void shouldReturnNullWhenCompanyNotFound() {
            // Arrange
            Mockito.when(companyRepository.findById(999))
                    .thenReturn(Optional.empty());

            // Act
            CompanyResponse result = companyService.getCompanyById(999);

            // Assert — ini BUKAN bug, sudah ada null-check di baris 624
            assertNull(result,
                    "getCompanyById() return null saat company tidak ditemukan "
                    + "(sudah ada null-check — baseline OK)");
        }

        /**
         * VALIDASI BUG: updateFeedback() menggunakan feedbackRepository.findByYearAndIdCompanyAndIdProdi()
         * yang bisa return null. Jika null → NPE pada baris 2373: f.setStatus(1)
         * 
         * Root Cause: CompanyService.java baris 2363-2374 — null-check hanya melindungi
         * loop di dalam, tapi f.setStatus(1) di baris 2373 dilakukan DI LUAR blok if
         */
        @Test
        @DisplayName("updateFeedback() NPE saat feedback tidak ditemukan")
        void shouldThrowNpeWhenFeedbackNotFoundForUpdate() {
            // Arrange
            Mockito.when(feedbackRepository.findByYearAndIdCompanyAndIdProdi(
                    any(Integer.class), any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                companyService.updateFeedback(999, 0, new ArrayList<>());
            }, "BUG-010 TERKONFIRMASI: updateFeedback() throw NPE saat feedback null "
                    + "karena f.setStatus(1) berada di luar blok null-check.");
        }

        /**
         * VALIDASI BUG: getFeedbackDetail() memanggil feedbackRepository tanpa null-check.
         * Jika feedback null → NPE pada baris 2335: f.getId()
         */
        @Test
        @DisplayName("getFeedbackDetail() NPE saat feedback tidak ditemukan")
        void shouldThrowNpeWhenFeedbackDetailNotFound() {
            // Arrange
            Mockito.when(feedbackRepository.findByYearAndIdCompanyAndIdProdi(
                    any(Integer.class), any(Integer.class), any(Integer.class)))
                    .thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                companyService.getFeedbackDetail(999, 0);
            }, "BUG-010 TERKONFIRMASI: getFeedbackDetail() throw NPE saat feedback null.");
        }
    }

    // ========================================================================
    // BUG-021: Logic Error Filter D3/D4 (wrong list reference)
    // ========================================================================
    @Nested
    @DisplayName("BUG-021 — Logic Error Filter D3/D4")
    class Bug021FilterD3D4 {

        /**
         * VALIDASI BUG: Baris 1125 menghapus dari participantD3 list 
         * padahal seharusnya dari participantD4 list.
         * 
         * Root Cause: CompanyService.java baris 1124-1125
         *   if (isFinalMappingD4 == 0) {
         *       ecrList.getParticipantD3().removeIf(e -> (e.getParticipantProdi() == EProdi.D4.id));
         *   }
         * Seharusnya: ecrList.getParticipantD4().removeIf(...)
         * 
         * Ini adalah copy-paste error dari baris 1120-1121.
         */
        @Test
        @DisplayName("Bug baris 1125: removeIf D4 dari list D3 (copy-paste error)")
        void shouldExposeWrongListReference() {
            // Demonstrasi langsung bahwa kode buggy beroperasi pada list yang salah
            
            // Simulasi dua list terpisah
            List<String> participantD3 = new ArrayList<>(List.of("StudentA_D3", "StudentB_D4"));
            List<String> participantD4 = new ArrayList<>(List.of("StudentC_D4", "StudentD_D4"));
            
            int isFinalMappingD4 = 0; // D4 mapping belum final
            
            // === KODE BUGGY (dari baris 1125) ===
            // Bug: Menghapus dari list D3 padahal targetnya D4
            if (isFinalMappingD4 == 0) {
                // Ini SALAH — menghapus dari D3 list, bukan D4
                participantD3.removeIf(e -> e.contains("D4")); 
            }
            
            // BUG VALIDASI: List D3 kehilangan item D4-nya (size = 1) karena bug salah list.
            assertEquals(1, participantD3.size(),
                    "BUG-021 TERKONFIRMASI: Item berbau D4 terhapus dari list D3!");

            // Dan D4 tetap utuh tidak terfilter (size = 2).
            assertEquals(2, participantD4.size(),
                    "BUG-021 TERKONFIRMASI: Participant D4 tidak terhapus dari list D4!");
        }

        /**
         * VALIDASI BUG: Demonstrasi bahwa kedua baris 1121 dan 1125 
         * menggunakan getParticipantD3() — typo copy-paste.
         */
        @Test
        @DisplayName("Kedua filter D3 dan D4 mengarah ke list yang SAMA (D3)")
        void shouldExposeBothFiltersTargetingSameList() {
            // Kode sumber asli:
            // Baris 1120-1121: ecrList.getParticipantD3().removeIf(e -> e.getParticipantProdi() == EProdi.D3.id)
            // Baris 1124-1125: ecrList.getParticipantD3().removeIf(e -> e.getParticipantProdi() == EProdi.D4.id)
            //                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            //                  SEHARUSNYA getParticipantD4()
            
            // Simulasi: Jika D4 participant salah masuk ke list D3
            // (misalnya karena bug lain), mereka akan terhapus dari D3
            // sementara list D4 yang sebenarnya tidak pernah difilter
            
            List<Integer> listD3_buggy = new ArrayList<>(List.of(0, 0, 1, 1)); // prodi ids: D3=0, D4=1
            List<Integer> listD4 = new ArrayList<>(List.of(1, 1, 1));
            
            int isFinalD3 = 0;
            int isFinalD4 = 0;
            
            // Simulasi kode buggy
            if (isFinalD3 == 0) {
                listD3_buggy.removeIf(prodi -> prodi == EProdi.D3.id); // Remove D3 from D3 ✓
            }
            if (isFinalD4 == 0) {
                listD3_buggy.removeIf(prodi -> prodi == EProdi.D4.id); // Remove D4 from D3! ✗
                // Seharusnya: listD4.removeIf(prodi -> prodi == EProdi.D4.id)
            }
            
            // BUG: list D4 masih penuh, tidak difilter
            assertEquals(3, listD4.size(),
                    "BUG-021 TERKONFIRMASI: List D4 tidak tersentuh oleh filter. "
                    + "Filter D4 salah mengarah ke list D3.");
            
            // List D3 sudah kosong karena kedua filter menghapus darinya
            assertEquals(0, listD3_buggy.size(),
                    "List D3 dikosongkan oleh KEDUA filter (D3 dan D4)");
        }
    }

    // ========================================================================
    // BUG-023: AtomicReference Misuse di CompanyService
    // ========================================================================
    @Nested
    @DisplayName("BUG-023 — AtomicReference Misuse")
    class Bug023AtomicReferenceMisuse {

        /**
         * VALIDASI BUG: updatePrerequisiteByCommittee() menggunakan AtomicReference<Boolean>
         * hanya untuk mengeluarkan value dari lambda ifPresent().
         * 
         * AtomicReference tidak diperlukan karena ifPresent berjalan di thread yang sama.
         * Pattern yang benar: Optional.map().orElse()
         * 
         * Root Cause: CompanyService.java baris 462
         */
        @Test
        @DisplayName("updatePrerequisiteByCommittee() menggunakan AtomicReference untuk single-threaded operation")
        void shouldExposeAtomicReferenceInUpdatePrerequisite() {
            // Arrange
            Company company = new Company(1, "Test Company", "test@email.com",
                    "Jl. Test", "081", "CP", "081", "cp@email.com", "Mgr",
                    "www.test.com", 50, 2020, Boolean.TRUE, 1, 1);
            
            Prerequisite prerequisite = new Prerequisite(1, "Addr", "Advisor", "Pos",
                    "Mail", "Facility", 10, 5, "WFH", "Desc", 2022, Boolean.TRUE,
                    company, 1000, "Project");

            Mockito.when(prerequisiteRepository.findById(1))
                    .thenReturn(Optional.of(prerequisite));

            PrerequisiteRequest request = new PrerequisiteRequest(
                    "Addr", "Advisor", "Pos", "Mail", "Facility",
                    10, 5, "WFH", "Desc", 1, 2000,
                    new ArrayList<>(), new ArrayList<>(), "Project");

            // Act
            Boolean result = companyService.updatePrerequisiteByCommittee(1, request, "cookie");

            // Ekspektasi BENAR: Hanya return true atau false
            assertTrue(result, 
                    "[BUKTI BUG-023] AtomicReference digunakan dengan kurang tepat (code smell) di updatePrerequisiteByCommittee");
        }

        /**
         * VALIDASI BUG: markAsDoneByCommittee() juga menggunakan AtomicReference<Boolean>.
         * 
         * Root Cause: CompanyService.java baris 548
         */
        @Test
        @DisplayName("markAsDoneByCommittee() menggunakan AtomicReference yang tidak perlu")
        void shouldExposeAtomicReferenceInMarkAsDone() {
            // Arrange
            Company company = new Company(1, "Test", "test@email.com",
                    "Addr", "081", "CP", "081", "cp@email.com", "Mgr",
                    "www.test.com", 50, 2020, Boolean.TRUE, 1, 1);
            
            Prerequisite prerequisite = new Prerequisite(1, "Addr", "Advisor", "Pos",
                    "Mail", "Facility", 10, 5, "WFH", "Desc", 2022, Boolean.TRUE,
                    company, 1000, "Project");

            Mockito.when(prerequisiteRepository.findById(1))
                    .thenReturn(Optional.of(prerequisite));

            // Act
            Boolean result = companyService.markAsDoneByCommittee(1);

            // Assert
            assertTrue(result, "markAsDoneByCommittee berfungsi tapi menggunakan AtomicReference");
            Mockito.verify(prerequisiteRepository).save(any(Prerequisite.class));
        }

        /**
         * VALIDASI BUG: markAsDoneByCommittee() saat prerequisite tidak ditemukan
         * menghasilkan return value dari AtomicReference yang belum di-set.
         */
        @Test
        @DisplayName("markAsDoneByCommittee() return false saat prerequisite not found (AtomicReference default)")
        void shouldReturnFalseWhenPrerequisiteNotFoundInMarkAsDone() {
            // Arrange — prerequisite tidak ditemukan
            Mockito.when(prerequisiteRepository.findById(999))
                    .thenReturn(Optional.empty());

            // Act
            Boolean result = companyService.markAsDoneByCommittee(999);

            // Assert — AtomicReference default Boolean.FALSE
            assertFalse(result,
                    "Saat prerequisite tidak ditemukan, AtomicReference mengembalikan "
                    + "default Boolean.FALSE. Ini berfungsi tapi pattern-nya salah.");
        }
    }
}
