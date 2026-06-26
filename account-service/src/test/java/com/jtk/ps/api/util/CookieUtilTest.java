package com.jtk.ps.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test untuk CookieUtil (account-service).
 *
 * Field @Value (accessTokenCookieName, refreshTokenCookieName) di-set manual
 * karena class diinstansiasi langsung dengan `new CookieUtil()`, bukan lewat
 * Spring context.
 *
 * Ref test case: UT-SEC-10, UT-SEC-11.
 */
@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    private CookieUtil cookieUtil;

    private static final String ACCESS_COOKIE_NAME = "accessToken";
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtil();
        ReflectionTestUtils.setField(cookieUtil, "accessTokenCookieName", ACCESS_COOKIE_NAME);
        ReflectionTestUtils.setField(cookieUtil, "refreshTokenCookieName", REFRESH_COOKIE_NAME);
    }

    // ===================================================================
    // UT-SEC-10 — createAccessTokenCookie(): flag keamanan cookie
    // Ref: ISS-NEW-03, ISS-012
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-10a: createAccessTokenCookie menghasilkan HttpOnly=true")
    void createAccessTokenCookie_shouldBeHttpOnly() {
        HttpCookie cookie = cookieUtil.createAccessTokenCookie("dummy-token-value", 900_000L);

        assertInstanceOf(ResponseCookie.class, cookie);
        ResponseCookie responseCookie = (ResponseCookie) cookie;
        assertTrue(responseCookie.isHttpOnly());
    }

    @Test
    @DisplayName("UT-SEC-10b: createAccessTokenCookie menghasilkan SameSite=Strict")
    void createAccessTokenCookie_shouldHaveSameSiteStrict() {
        ResponseCookie responseCookie =
                (ResponseCookie) cookieUtil.createAccessTokenCookie("dummy-token-value", 900_000L);

        assertEquals("Strict", responseCookie.getSameSite());
    }

    @Test
    @DisplayName("TEMUAN UT-SEC-10c: createAccessTokenCookie BELUM mengaktifkan flag Secure")
    void createAccessTokenCookie_secureFlag_currentlyDisabled() {
        ResponseCookie responseCookie =
                (ResponseCookie) cookieUtil.createAccessTokenCookie("dummy-token-value", 900_000L);

        // Per source CookieUtil.java baris 25: `.secure(true)` masih di-comment.
        // Test ini sengaja menegaskan kondisi SAAT INI (gap), bukan kondisi ideal.
        // Begitu tim Security mengaktifkan .secure(true) untuk profil prod,
        // assertion ini HARUS diubah menjadi assertTrue(...) dan ditandai selesai.
        assertFalse(responseCookie.isSecure(),
                "Flag Secure pada cookie masih nonaktif (.secure(true) di-comment di CookieUtil.java). " +
                        "Sesuai SRS BAB IV NFR-S-01 & dokumen perancangan T-06, flag ini wajib aktif minimal di profil production.");
    }

    @Test
    @DisplayName("UT-SEC-10d: createAccessTokenCookie menggunakan nama cookie dan duration yang benar")
    void createAccessTokenCookie_shouldUseConfiguredNameAndDuration() {
        long duration = 900_000L;
        ResponseCookie responseCookie =
                (ResponseCookie) cookieUtil.createAccessTokenCookie("dummy-token-value", duration);

        assertEquals(ACCESS_COOKIE_NAME, responseCookie.getName());
        assertEquals("dummy-token-value", responseCookie.getValue());
        assertEquals(duration, responseCookie.getMaxAge().getSeconds());
        assertEquals("/", responseCookie.getPath());
    }

    @Test
    @DisplayName("UT-SEC-10e: createRefreshTokenCookie juga HttpOnly + SameSite=Strict, nama sesuai refreshTokenCookieName")
    void createRefreshTokenCookie_shouldBeHttpOnlyAndSameSiteStrict() {
        ResponseCookie responseCookie =
                (ResponseCookie) cookieUtil.createRefreshTokenCookie("dummy-refresh-value", 604_800_000L);

        assertTrue(responseCookie.isHttpOnly());
        assertEquals("Strict", responseCookie.getSameSite());
        assertEquals(REFRESH_COOKIE_NAME, responseCookie.getName());
    }

    // ===================================================================
    // UT-SEC-11 — deleteAccessTokenCookie()/deleteRefreshTokenCookie(): expire saat logout
    // Ref: ISS-012, S2-T12
    // ===================================================================
    @Test
    @DisplayName("UT-SEC-11a: deleteAccessTokenCookie menghasilkan Max-Age=0 dan tetap HttpOnly + SameSite=Strict")
    void deleteAccessTokenCookie_shouldExpireImmediately() {
        ResponseCookie responseCookie = (ResponseCookie) cookieUtil.deleteAccessTokenCookie();

        assertEquals(0, responseCookie.getMaxAge().getSeconds());
        assertTrue(responseCookie.isHttpOnly());
        assertEquals("Strict", responseCookie.getSameSite());
        assertEquals(ACCESS_COOKIE_NAME, responseCookie.getName());
        assertEquals("", responseCookie.getValue());
    }

    @Test
    @DisplayName("UT-SEC-11b: deleteRefreshTokenCookie menghasilkan Max-Age=0 dan tetap HttpOnly + SameSite=Strict")
    void deleteRefreshTokenCookie_shouldExpireImmediately() {
        ResponseCookie responseCookie = (ResponseCookie) cookieUtil.deleteRefreshTokenCookie();

        assertEquals(0, responseCookie.getMaxAge().getSeconds());
        assertTrue(responseCookie.isHttpOnly());
        assertEquals("Strict", responseCookie.getSameSite());
        assertEquals(REFRESH_COOKIE_NAME, responseCookie.getName());
    }
}