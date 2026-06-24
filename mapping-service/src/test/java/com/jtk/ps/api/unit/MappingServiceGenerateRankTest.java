package com.jtk.ps.api.unit;

import com.jtk.ps.api.dto.ResponseList;
import com.jtk.ps.api.dto.ranking.CompanyReqResponse;
import com.jtk.ps.api.dto.ranking.CompanySelection;
import com.jtk.ps.api.dto.ranking.ParticipantReqResponse;
import com.jtk.ps.api.model.CriteriaMapping;
import com.jtk.ps.api.model.UtilityDate;
import com.jtk.ps.api.repository.CriteriaMappingRepository;
import com.jtk.ps.api.repository.FinalMappingRepository;
import com.jtk.ps.api.repository.ParticipantRankingRepository;
import com.jtk.ps.api.repository.UtilityDateRepository;
import com.jtk.ps.api.repository.UtilityRepository;
import com.jtk.ps.api.service.MappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ============================================================================
 * POST-FIX VALIDATION TEST — MappingService#generateRank()
 * ============================================================================
 *
 * Tujuan: Memvalidasi bahwa BUG-023 (bagian kedua) sudah diperbaiki dengan benar:
 * generateRank() TIDAK LAGI memanggil WebClient.block() dari thread Tomcat,
 * melainkan menggunakan RestTemplate.exchange() yang sinkron dan aman dipanggil
 * dari servlet thread biasa.
 *
 * Cara membaca hasil test:
 * - Test yang PASS = fix sudah benar (tidak ada lagi WebClient/block(), RestTemplate
 *   dipanggil dengan benar, dan hasil akhir method tetap sesuai ekspektasi)
 * - Test yang FAIL = migrasi belum lengkap atau ada regresi pada flow generateRank()
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MappingServiceGenerateRankTest {

    @InjectMocks
    private MappingService mappingService;

    @Mock
    private FinalMappingRepository finalMappingRepository;

    @Mock
    private CriteriaMappingRepository criteriaMappingRepository;

    @Mock
    private ParticipantRankingRepository participantRankingRepository;

    @Mock
    private UtilityDateRepository utilityDateRepository;

    @Mock
    private UtilityRepository utilityRepository;

    @Mock
    private RestTemplate restTemplate;

    private static final String COOKIE = "test-cookie";
    private static final Integer ID_PRODI = 1;

    @BeforeEach
    void setUp() {
        // 9 baris CriteriaMapping wajib ada (id 1-9) karena generateRank() mengakses
        // bobotCriteria.get(0)..get(8) tanpa null-check — root cause IndexOutOfBoundsException
        // kalau salah satu kriteria hilang. Ini bukan bagian dari bug yang diperbaiki,
        // hanya prasyarat agar method bisa berjalan sampai akhir.
        List<CriteriaMapping> criteriaMappings = new ArrayList<>();
        for (int id = 1; id <= 9; id++) {
            criteriaMappings.add(new CriteriaMapping(id, "Criteria " + id, false, 10f));
        }
        when(criteriaMappingRepository.findAll()).thenReturn(criteriaMappings);

        // Tidak ada UtilityDate existing -> method akan masuk cabang "create new"
        when(utilityDateRepository.findByIdUtilityAndYearAndProdiId(anyInt(), anyInt(), anyInt()))
                .thenReturn(null);
    }

    /**
     * Menyiapkan stub RestTemplate.exchange() untuk ketiga endpoint yang dipanggil
     * generateRank(): req-company, cv-interest-participant, company-selection/mapping.
     * Data dibuat minimal (jobscope & competence kosong) -- cukup untuk lolos dari
     * seluruh loop kalkulasi SAW tanpa NPE, tanpa perlu menguji hasil skornya.
 * @param restTemplate2 TODO
     */
    private void stubAllRestTemplateCalls(RestTemplate restTemplate2) {
        CompanyReqResponse company = new CompanyReqResponse(
                1, "Test Company", 1, Collections.emptyList(), Collections.emptyList());
        ResponseList<CompanyReqResponse> companyBody =
                new ResponseList<>(List.of(company), 200, "OK");
        when(restTemplate2.exchange(
                eq("http://company-service/company/req-company"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(companyBody, HttpStatus.OK));

        ParticipantReqResponse participant = new ParticipantReqResponse(
                1, 1, Collections.emptyList(), Collections.emptyList());
        ResponseList<ParticipantReqResponse> participantBody =
                new ResponseList<>(List.of(participant), 200, "OK");
        when(restTemplate.exchange(
                eq("http://participant-service/participant/cv-interest-participant"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(participantBody, HttpStatus.OK));

        CompanySelection selection = new CompanySelection(1, 1, 1, 1);
        ResponseList<CompanySelection> selectionBody =
                new ResponseList<>(List.of(selection), 200, "OK");
        when(restTemplate.exchange(
                eq("http://participant-service/participant/company-selection/mapping"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(selectionBody, HttpStatus.OK));
    }

    // ========================================================================
    // BUG-023 (bagian 2): WebClient.block() -> RestTemplate.exchange()
    // ========================================================================
    @Nested
    @DisplayName("BUG-023 — WebClient.block() Migration to RestTemplate")
    class Bug023WebClientBlockFix {

        /**
         * VALIDASI FIX: generateRank() berhasil mengambil data dari tiga endpoint
         * REST menggunakan RestTemplate.exchange() (sinkron, aman dipanggil dari
         * thread Tomcat) dan mengembalikan Boolean.TRUE saat seluruh proses sukses.
         */
        @Test
        @DisplayName("generateRank() mengembalikan TRUE dan memanggil RestTemplate untuk ketiga endpoint")
        void shouldReturnTrueAndCallRestTemplateForAllThreeEndpoints() {
            // Arrange
            stubAllRestTemplateCalls(restTemplate);

            // Act
            Boolean result = mappingService.generateRank(COOKIE, ID_PRODI);

            // Assert
            assertTrue(result,
                    "generateRank() harus mengembalikan TRUE saat ketiga endpoint REST berhasil diambil");

            verify(restTemplate, times(1)).exchange(
                    eq("http://company-service/company/req-company"),
                    eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
            verify(restTemplate, times(1)).exchange(
                    eq("http://participant-service/participant/cv-interest-participant"),
                    eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
            verify(restTemplate, times(1)).exchange(
                    eq("http://participant-service/participant/company-selection/mapping"),
                    eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        }

        /**
         * VALIDASI FIX: cookie yang diterima generateRank() benar-benar diteruskan
         * sebagai header pada request REST -- memastikan migrasi dari
         * webClient.header(COOKIE, cookie) ke HttpHeaders/HttpEntity tidak
         * menghilangkan propagasi cookie.
         */
        @Test
        @DisplayName("Cookie diteruskan dengan benar sebagai HTTP header pada setiap RestTemplate call")
        void shouldPropagateCookieHeaderToRestTemplate() {
            // Arrange
            stubAllRestTemplateCalls(restTemplate);

            // Act
            mappingService.generateRank(COOKIE, ID_PRODI);

            // Assert
            ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate, times(3)).exchange(
                    any(String.class), eq(HttpMethod.GET), entityCaptor.capture(), any(ParameterizedTypeReference.class));

            for (HttpEntity<?> capturedEntity : entityCaptor.getAllValues()) {
                List<String> cookieHeader = capturedEntity.getHeaders().get("cookie");
                assertNotNull(cookieHeader, "Header cookie harus ada pada setiap request");
                assertTrue(cookieHeader.contains(COOKIE),
                        "Header cookie yang dikirim harus sama dengan cookie yang diterima generateRank()");
            }
        }

        /**
         * VALIDASI FIX: tidak ada lagi field WebClient.Builder pada MappingService.
         * Sebelum fix, field ini ada dan dipakai untuk membangun Mono yang di-block().
         * Setelah fix, dependency ini seharusnya sudah dihapus seluruhnya.
         */
        @Test
        @DisplayName("MappingService tidak lagi memiliki field WebClient.Builder")
        void shouldNotHaveWebClientBuilderFieldAnymore() {
            boolean hasWebClientField = false;
            for (Field field : MappingService.class.getDeclaredFields()) {
                if (field.getType().getName().contains("WebClient")) {
                    hasWebClientField = true;
                    break;
                }
            }
            assertFalse(hasWebClientField,
                    "[BUKTI FIX BUG-023] MappingService seharusnya tidak lagi memiliki "
                    + "field WebClient.Builder setelah migrasi ke RestTemplate");
        }

        /**
         * VALIDASI FIX: generateRank() tetap menyelesaikan side-effect-nya
         * (menyimpan ranking baru dan memperbarui UtilityDate) setelah migrasi,
         * memastikan tidak ada regresi pada langkah akhir method akibat
         * perubahan mekanisme fetch data.
         */
        @Test
        @DisplayName("generateRank() tetap menyimpan participant ranking dan utility date setelah migrasi")
        void shouldStillPersistRankingsAndUtilityDateAfterMigration() {
            // Arrange
            stubAllRestTemplateCalls(restTemplate);

            // Act
            mappingService.generateRank(COOKIE, ID_PRODI);

            // Assert
            verify(participantRankingRepository, times(1)).deleteByYear(any(Integer.class), eq(ID_PRODI));
            verify(participantRankingRepository, times(1)).saveAll(any(List.class));
            verify(utilityDateRepository, times(1)).save(any(UtilityDate.class));
        }

        /**
         * VALIDASI FIX: ketika salah satu endpoint REST gagal (mengembalikan body null),
         * generateRank() harus melempar exception yang jelas (NullPointerException dari
         * Objects.requireNonNull) -- bukan diam-diam mengembalikan data kosong/salah
         * seperti yang bisa terjadi pada error handling Mono yang tidak tepat.
         */
        @Test
        @DisplayName("generateRank() melempar NullPointerException saat response body null")
        void shouldThrowWhenResponseBodyIsNull() {
            // Arrange
            when(restTemplate.exchange(
                    eq("http://company-service/company/req-company"),
                    eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                    .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

            // Act + Assert
            assertThrows(NullPointerException.class,
                    () -> mappingService.generateRank(COOKIE, ID_PRODI),
                    "Saat body response null, Objects.requireNonNull harus melempar NPE "
                    + "alih-alih melanjutkan dengan data tidak valid");
        }
    }
}
