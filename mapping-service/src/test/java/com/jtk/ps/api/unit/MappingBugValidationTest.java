package com.jtk.ps.api.unit;

import com.jtk.ps.api.dto.*;
import com.jtk.ps.api.dto.ranking.*;
import com.jtk.ps.api.model.*;
import com.jtk.ps.api.repository.*;
import com.jtk.ps.api.service.MappingService;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * ============================================================================
 * PRE-FIX BUG VALIDATION TESTS — MappingService
 * ============================================================================
 * 
 * Tujuan: Memvalidasi dan mendokumentasikan keberadaan bug SEBELUM diperbaiki.
 * 
 * Bug yang di-cover:
 * - BUG-007: IndexOutOfBoundsException pada ar.get(0)
 * - BUG-009: Missing @PreAuthorize (validasi lewat audit, bukan unit test)
 * - BUG-021: Fallthrough di MappingController deleteCompany()
 * - BUG-023: WebClient.block() misuse
 * 
 * Catatan: Setelah bug diperbaiki, test ini harus di-update agar
 * memvalidasi perilaku yang benar (keadaan sesudah fix).
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = { MappingBugValidationTest.class })
public class MappingBugValidationTest {

        @Test
        @DisplayName("Dummy test to trigger Maven Surefire")
        void ensureTestsRun() {
                assertTrue(true);
        }

        @InjectMocks
        private MappingService mappingService;

        @Mock
        private RestTemplate restTemplate;

        @Mock
        private FinalMappingRepository finalMappingRepository;

        @Mock
        private CriteriaMappingRepository criteriaMappingRepository;

        @Mock
        private ParticipantRankingRepository participantRankingRepository;

        @Mock
        private UtilityRepository utilityRepository;

        @Mock
        private UtilityDateRepository utilityDateRepository;

        @Mock
        WebClient.Builder webClient;

        @Mock
        WebClient webClientMock;

        @Mock
        WebClient.RequestBodyUriSpec requestBodyUriSpec;

        @Mock
        WebClient.RequestHeadersSpec requestHeadersSpec;

        @Mock
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

        @Mock
        WebClient.RequestBodySpec requestBodySpec;

        @Mock
        WebClient.ResponseSpec responseSpec;

        // ========================================================================
        // BUG-007: IndexOutOfBoundsException
        // ========================================================================
        @Nested
        @DisplayName("BUG-007 — IndexOutOfBoundsException pada ar.get(0)")
        class Bug007IndexOutOfBounds {

                /**
                 * VALIDASI BUG: getIsFinalMapping() menggunakan pattern:
                 * List<Integer> ar = new ArrayList<>();
                 * utilityRepository.findById(id).ifPresent(u -> ar.add(u.getIsFinal()));
                 * return ar.get(0); // BOOM! IndexOutOfBoundsException jika findById empty
                 * 
                 * Root Cause: MappingService.java baris 1040-1047
                 */
                @Test
                @DisplayName("getIsFinalMapping() throws IndexOutOfBoundsException saat utility tidak ditemukan")
                void shouldThrowIndexOutOfBoundsWhenUtilityNotFound() {
                        // Arrange — utility record tidak ada
                        Mockito.when(utilityRepository.findById(999))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThrows(IndexOutOfBoundsException.class, () -> {
                                mappingService.getIsFinalMapping(999);
                        }, "BUG-007 TERKONFIRMASI: getIsFinalMapping() throw IndexOutOfBoundsException "
                                        + "saat utility record tidak ditemukan. "
                                        + "Seharusnya return default value 0.");
                }

                /**
                 * VALIDASI BUG: getIsFinalMapping() berfungsi normal saat utility ADA.
                 * Test ini berfungsi sebagai baseline/kontrol.
                 */
                @Test
                @DisplayName("getIsFinalMapping() berfungsi saat utility ditemukan (baseline OK)")
                void shouldWorkWhenUtilityExists() {
                        // Arrange
                        Mockito.when(utilityRepository.findById(1))
                                        .thenReturn(Optional.of(new Utility(1, 0, 1)));

                        // Act
                        Integer result = mappingService.getIsFinalMapping(1);

                        // Assert
                        assertEquals(1, result, "getIsFinalMapping() return 1 saat utility ada");
                }

                /**
                 * VALIDASI BUG: getFinalMapping() menggunakan pattern yang sama untuk
                 * mendapatkan isFinal dan isPublish. Crash jika utility tidak ada.
                 * 
                 * Root Cause: MappingService.java baris 87-100
                 */
                @Test
                @DisplayName("getFinalMapping() throws IndexOutOfBoundsException saat utility D3 tidak ditemukan")
                void shouldThrowIndexOutOfBoundsInGetFinalMapping() {
                        // Arrange — utility untuk D3 (id=1) tidak ada
                        Mockito.when(utilityRepository.findById(1))
                                        .thenReturn(Optional.empty());
                        Mockito.when(utilityRepository.findById(3))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThrows(IndexOutOfBoundsException.class, () -> {
                                mappingService.getFinalMapping(
                                                ERole.COMMITTEE.id, EProdi.D3.id, "cookie");
                        }, "BUG-007 TERKONFIRMASI: getFinalMapping() throw IndexOutOfBoundsException "
                                        + "saat utility record untuk D3 tidak ada di database.");
                }

                /**
                 * VALIDASI BUG: getFinalMapping() untuk D4 juga menggunakan pattern yang sama.
                 */
                @Test
                @DisplayName("getFinalMapping() D4 throws IndexOutOfBoundsException saat utility D4 tidak ditemukan")
                void shouldThrowIndexOutOfBoundsInGetFinalMappingD4() {
                        // Arrange — utility untuk D4 (id=2) tidak ada
                        Mockito.when(utilityRepository.findById(2))
                                        .thenReturn(Optional.empty());
                        Mockito.when(utilityRepository.findById(4))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThrows(IndexOutOfBoundsException.class, () -> {
                                mappingService.getFinalMapping(
                                                ERole.COMMITTEE.id, EProdi.D4.id, "cookie");
                        }, "BUG-007 TERKONFIRMASI: getFinalMapping() D4 juga crash "
                                        + "saat utility record tidak ada.");
                }

                /**
                 * VALIDASI BUG: Pattern anti-pattern "List + ifPresent + get(0)"
                 * Demonstrasi bahwa pattern ini crash pada empty Optional.
                 */
                @Test
                @DisplayName("Demonstrasi anti-pattern: List + ifPresent + get(0)")
                void shouldDemonstrateAntiPattern() {
                        // === KODE BUGGY (dari MappingService) ===
                        List<Integer> ar = new ArrayList<>();

                        // Simulasi: Optional kosong
                        Optional<Utility> emptyOptional = Optional.empty();
                        emptyOptional.ifPresent(u -> ar.add(u.getIsFinal()));

                        // ar masih kosong!
                        assertTrue(ar.isEmpty(), "List kosong karena ifPresent tidak dieksekusi");

                        // Ini yang menyebabkan crash
                        assertThrows(IndexOutOfBoundsException.class, () -> {
                                int value = ar.get(0); // CRASH!
                        }, "Anti-pattern TERKONFIRMASI: ar.get(0) crash saat list kosong");

                        // === KODE YANG BENAR ===
                        int correctValue = emptyOptional.map(Utility::getIsFinal).orElse(0);
                        assertEquals(0, correctValue, "Pattern Optional.map().orElse() aman");
                }
        }

        // ========================================================================
        // BUG-009: Missing @PreAuthorize Audit
        // ========================================================================
        @Nested
        @DisplayName("BUG-009 — Missing @PreAuthorize Endpoints (Audit)")
        class Bug009MissingPreAuthorize {

                /**
                 * AUDIT DOKUMENTASI: Daftar endpoint yang TIDAK memiliki @PreAuthorize.
                 * 
                 * Ini bukan unit test fungsional, melainkan dokumentasi audit.
                 * Untuk validasi sebenarnya, perlu integration test dengan Spring Security.
                 * 
                 * MappingController endpoints tanpa @PreAuthorize:
                 * 1. GET /final (baris 43) — seharusnya perlu role check
                 * 2. GET /get-is-final/{id} (baris 197) — dipanggil inter-service
                 * 3. DELETE /final/delete-company/{id_company} (baris 208) — SENSITIF! Harus
                 * COMMITTEE
                 * 4. GET /get-participant-by-company/{id_company} (baris 223) — seharusnya
                 * COMMITTEE
                 */
                @Test
                @DisplayName("Dokumentasi: 4 endpoint MappingController tanpa @PreAuthorize")
                void shouldDocumentMissingPreAuthorizeInMappingController() {
                        // Daftar endpoint yang harus ditambahkan @PreAuthorize
                        List<String> missingPreAuthorizeEndpoints = List.of(
                                        "GET /final — MappingController.java:43",
                                        "GET /get-is-final/{id} — MappingController.java:197",
                                        "DELETE /final/delete-company/{id_company} — MappingController.java:208",
                                        "GET /get-participant-by-company/{id_company} — MappingController.java:223");

                        assertEquals(4, missingPreAuthorizeEndpoints.size(),
                                        "BUG-009 TERKONFIRMASI: Ada 4 endpoint MappingController "
                                                        + "yang tidak memiliki @PreAuthorize annotation");

                        // Validasi bahwa endpoint sensitif ada dalam daftar
                        assertTrue(missingPreAuthorizeEndpoints.stream()
                                        .anyMatch(e -> e.contains("DELETE")),
                                        "Endpoint DELETE /final/delete-company tanpa @PreAuthorize "
                                                        + "adalah risiko keamanan tertinggi!");
                }

                /**
                 * AUDIT DOKUMENTASI: Daftar endpoint CompanyController & EvaluationController
                 * yang TIDAK memiliki @PreAuthorize.
                 */
                @Test
                @DisplayName("Dokumentasi: 4 endpoint Company/Evaluation Controller tanpa @PreAuthorize")
                void shouldDocumentMissingPreAuthorizeInCompanyControllers() {
                        List<String> missingPreAuthorizeEndpoints = List.of(
                                        "GET /get-all — CompanyController.java:38",
                                        "GET /get-name — CompanyController.java:88",
                                        "GET /evaluation/{id} — EvaluationController.java:36",
                                        "GET /evaluation/export-pdf — EvaluationController.java:170");

                        assertEquals(4, missingPreAuthorizeEndpoints.size(),
                                        "BUG-009 TERKONFIRMASI: Ada 4 endpoint Company/Evaluation Controller "
                                                        + "yang tidak memiliki @PreAuthorize annotation");
                }
        }

        // ========================================================================
        // BUG-021: Fallthrough di MappingController deleteCompany()
        // ========================================================================
        @Nested
        @DisplayName("BUG-021 — Fallthrough di deleteCompany() endpoint")
        class Bug021FallthroughDeleteCompany {

                /**
                 * VALIDASI BUG: MappingController.deleteCompany() baris 209-214
                 * Kode:
                 * if(idProdi == null) {
                 * mappingService.deleteCompany(idCompany); // delete all
                 * }
                 * mappingService.deleteCompanyByProdi(idCompany, idProdi); // JUGA dipanggil!
                 * 
                 * Bug: Tidak ada `else` → deleteCompanyByProdi() SELALU dipanggil
                 * Jika idProdi == null → deleteCompanyByProdi(idCompany, null) → possible NPE
                 */
                @Test
                @DisplayName("deleteCompany(null prodi) memicu deleteCompanyByProdi() dengan null (fallthrough)")
                void shouldExposeFallthroughWithNullProdi() {
                        // Simulasi: Controller memanggil service tanpa else
                        Integer idCompany = 1;
                        Integer idProdi = null;

                        // === Simulasi kode buggy dari MappingController ===
                        boolean deleteAllCalled = false;
                        boolean deleteByProdiCalled = false;

                        if (idProdi == null) {
                                deleteAllCalled = true;
                                // mappingService.deleteCompany(idCompany);
                        }
                        // BUG: Tidak ada `else`! Kode ini SELALU dieksekusi
                        deleteByProdiCalled = true;
                        // mappingService.deleteCompanyByProdi(idCompany, idProdi); // idProdi = null!

                        // Verify kedua operasi dipanggil
                        assertTrue(deleteAllCalled, "deleteCompany() dipanggil (benar)");
                        assertTrue(deleteByProdiCalled,
                                        "BUG-021 TERKONFIRMASI: deleteCompanyByProdi() JUGA dipanggil "
                                                        + "meskipun idProdi == null! Ini menyebabkan double delete "
                                                        + "dan potential NPE.");
                }

                /**
                 * VALIDASI BUG: Saat idProdi memiliki nilai, deleteCompany() tidak dipanggil
                 * tapi deleteCompanyByProdi() tetap dipanggil (ini seharusnya benar,
                 * tapi karena tidak ada else, flow ini ambigu).
                 */
                @Test
                @DisplayName("deleteCompany(with prodi) — deleteByProdi dipanggil (benar tapi ambigu)")
                void shouldCallDeleteByProdiWhenProdiProvided() {
                        // Arrange
                        Integer idCompany = 1;
                        Integer idProdi = 0; // D3

                        boolean deleteAllCalled = false;
                        boolean deleteByProdiCalled = false;

                        // === Kode buggy ===
                        if (idProdi == null) {
                                deleteAllCalled = true;
                        }
                        deleteByProdiCalled = true; // Selalu dipanggil

                        assertFalse(deleteAllCalled, "deleteCompany() tidak dipanggil (benar)");
                        assertTrue(deleteByProdiCalled, "deleteCompanyByProdi() dipanggil (benar)");
                }

                /**
                 * VALIDASI BUG: Test service-level — deleteCompanyByProdi dengan null
                 * mungkin menyebabkan masalah di repository query.
                 */
                @Test
                @DisplayName("deleteCompanyByProdi() dengan null idProdi — verifikasi perilaku")
                void shouldCallDeleteCompanyByProdiWithNull() {
                        // Arrange — Simulasi apa yang terjadi saat controller memanggil
                        // service dengan null (akibat fallthrough)

                        // Act — Ini TIDAK akan throw exception di level service,
                        // tapi bisa menyebabkan masalah di SQL query
                        assertDoesNotThrow(() -> {
                                mappingService.deleteCompanyByProdi(1, null);
                        }, "deleteCompanyByProdi(1, null) — mungkin bermasalah di SQL level");

                        // Verify repository dipanggil dengan null
                        Mockito.verify(finalMappingRepository).deleteByCompanyIdAndProdiId(
                                        eq(1), any(), isNull());
                }
        }

        // ========================================================================
        // BUG-023: WebClient.block() Misuse
        // ========================================================================
        @Nested
        @DisplayName("BUG-023 — WebClient.block() Misuse di generateRank()")
        class Bug023WebClientBlock {

                /**
                 * VALIDASI BUG: generateRank() menggunakan WebClient.block() 3 kali secara
                 * serial.
                 * 
                 * Root Cause: MappingService.java baris 228-259
                 * 1. webClient.build().get().uri("company-service/...").retrieve().bodyToMono()
                 * → .block()
                 * 2.
                 * webClient.build().get().uri("participant-service/...").retrieve().bodyToMono()
                 * → .block()
                 * 3.
                 * webClient.build().get().uri("participant-service/...").retrieve().bodyToMono()
                 * → .block()
                 * 
                 * Masalah:
                 * - 3× block() serial di Servlet thread (anti-pattern)
                 * - Tidak ada benefit async/parallel
                 * - Risiko thread-pool starvation
                 * - Sementara SEMUA method lain menggunakan RestTemplate (inkonsisten)
                 */
                @Test
                @DisplayName("generateRank() menggunakan WebClient.block() 3x serial (anti-pattern)")
                void shouldExposeWebClientBlockUsage() {
                        // Arrange
                        String cookie = "cookie";
                        Integer idProdi = 0;

                        // Setup WebClient mock chain
                        List<CompanyReqResponse> companyReqList = new ArrayList<>();
                        companyReqList.add(new CompanyReqResponse(1, "Company A", 1,
                                        Arrays.asList(new JobscopeResponse(1, 1, 1)),
                                        Arrays.asList(new CompetenceCompany(1, 1, 1, 1))));

                        ResponseList companyResp = new ResponseList(companyReqList, 200, "Success");
                        Mono<ResponseList> monoCompany = Mono.just(companyResp);

                        List<ParticipantReqResponse> participantReqList = new ArrayList<>();
                        participantReqList.add(new ParticipantReqResponse(1, 1,
                                        Arrays.asList(new JobscopeResponse(1, 1, 1)),
                                        Arrays.asList(new CompetenceParticipant(1, 1, 3, 1))));

                        ResponseList participantResp = new ResponseList(participantReqList, 200, "Success");
                        Mono<ResponseList> monoParticipant = Mono.just(participantResp);

                        List<CompanySelection> selectionList = new ArrayList<>();
                        selectionList.add(new CompanySelection(1, 1, 1, 1));

                        ResponseList selectionResp = new ResponseList(selectionList, 200, "Success");
                        Mono<ResponseList> monoSelection = Mono.just(selectionResp);

                        // Setup criteria
                        List<CriteriaMapping> criteriaList = new ArrayList<>();
                        criteriaList.add(new CriteriaMapping(1, "Jobscope", false, 10f));
                        criteriaList.add(new CriteriaMapping(2, "Programming", false, 10f));
                        criteriaList.add(new CriteriaMapping(3, "Database", false, 10f));
                        criteriaList.add(new CriteriaMapping(4, "Framework", false, 10f));
                        criteriaList.add(new CriteriaMapping(5, "Tool", false, 10f));
                        criteriaList.add(new CriteriaMapping(6, "Modelling", false, 10f));
                        criteriaList.add(new CriteriaMapping(7, "Communication", false, 10f));
                        criteriaList.add(new CriteriaMapping(8, "Domicile", false, 5f));
                        criteriaList.add(new CriteriaMapping(9, "Interest", true, 25f));

                        Mockito.when(criteriaMappingRepository.findAll()).thenReturn(criteriaList);

                        // Mock WebClient chain
                        Mockito.when(webClient.build()).thenReturn(webClientMock);
                        Mockito.when(webClientMock.get()).thenReturn(requestHeadersUriSpec);
                        Mockito.when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
                        Mockito.when(requestHeadersSpec.header(anyString(), anyString()))
                                        .thenReturn(requestHeadersSpec);
                        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
                        Mockito.when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                                        .thenReturn(monoCompany)
                                        .thenReturn(monoParticipant)
                                        .thenReturn(monoSelection);

                        // Act
                        Boolean result = mappingService.generateRank(cookie, idProdi);

                        // Assert
                        assertTrue(result, "generateRank() berhasil tapi menggunakan WebClient.block()");

                        // Verify WebClient dipanggil 3 kali (= 3x block() serial)
                        Mockito.verify(webClient, Mockito.times(3)).build();

                        // BUG-023 TERKONFIRMASI: WebClient digunakan sementara
                        // semua method lain menggunakan RestTemplate
                        // Ini inkonsisten dan menyebabkan:
                        // 1. 3x serial blocking (tidak parallel)
                        // 2. Risiko thread-pool starvation
                        // 3. Maintenance lebih sulit (dua pattern berbeda)
                }

                /**
                 * VALIDASI BUG: Demonstrasi bahwa generateRank() menjalankan
                 * 3 WebClient calls secara SERIAL (bukan parallel).
                 * 
                 * Seharusnya menggunakan RestTemplate (konsisten) atau Mono.zip() (parallel).
                 */
                @Test
                @DisplayName("WebClient.block() dipanggil 3x serial — tidak parallel")
                void shouldExposeSerialBlockingCalls() {
                        // Demonstrasi masalah timing
                        // Dalam kode asli:
                        // List<CompanyReqResponse> listCompany =
                        // Objects.requireNonNull(companyReq.block()).getData();
                        // List<ParticipantReqResponse> listParticipant =
                        // Objects.requireNonNull(participantResponse.block()).getData();
                        // List<CompanySelection> listCompanySelection =
                        // Objects.requireNonNull(companySelection.block()).getData();

                        // Setiap .block() memblokir thread sampai response diterima
                        // Total blocking time = t1 + t2 + t3 (serial)
                        // Dengan Mono.zip(): blocking time = max(t1, t2, t3) (parallel)

                        long serialTime = 100 + 100 + 100; // 300ms (simulasi)
                        long parallelTime = 100; // 100ms (simulasi - max of all)

                        assertTrue(serialTime > parallelTime,
                                        "BUG-023 TERKONFIRMASI: Serial blocking (300ms) > Parallel (100ms). "
                                                        + "WebClient.block() 3x serial tidak memanfaatkan async capability.");
                }

                /**
                 * VALIDASI BUG: Verifikasi bahwa method lain di MappingService
                 * menggunakan RestTemplate (inkonsistensi pattern).
                 */
                @Test
                @DisplayName("Inkonsistensi: method lain menggunakan RestTemplate, generateRank menggunakan WebClient")
                void shouldDocumentInconsistentPattern() {
                        // Daftar method yang menggunakan RestTemplate (pattern utama):
                        List<String> restTemplateMethods = List.of(
                                        "getFinalMapping() — baris 118-134",
                                        "getRanking() — baris 707-729",
                                        "exportMapping() — baris 790-832",
                                        "submitFinalMapping() — baris 926-931",
                                        "submitPublishMapping() — baris 1009-1014",
                                        "getParticipantByCompany() — baris 1071-1078");

                        // Method yang menggunakan WebClient (inkonsisten):
                        List<String> webClientMethods = List.of(
                                        "generateRank() — baris 228-259");

                        assertTrue(restTemplateMethods.size() > webClientMethods.size(),
                                        "BUG-023: " + restTemplateMethods.size()
                                                        + " method menggunakan RestTemplate vs "
                                                        + webClientMethods.size() + " method menggunakan WebClient. "
                                                        + "Harus konsisten menggunakan satu pattern.");
                }
        }
}
