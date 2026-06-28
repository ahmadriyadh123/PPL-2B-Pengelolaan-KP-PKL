package com.kppkl.authcommons.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S3-T01 — Unit test CookieTokenReader.
 *
 * Skenario yang diuji:
 * - cookie ada dan berisi token → Optional berisi token
 * - tidak ada cookie → Optional.empty()
 * - cookie ada tapi nilainya kosong → Optional.empty()
 * - banyak cookie, hanya yang sesuai nama yang dibaca
 */
class CookieTokenReaderTest {

    private static final String COOKIE_NAME = "accessToken";
    private CookieTokenReader reader;

    @BeforeEach
    void setUp() {
        reader = new CookieTokenReader(COOKIE_NAME);
    }

    @Test
    @DisplayName("cookie accessToken ada → Optional berisi token")
    void readAccessToken_cookiePresent_returnsToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new javax.servlet.http.Cookie(COOKIE_NAME, "eyJhbGciOiJIUzUxMiJ9.test.sig")
        );

        Optional<String> result = reader.readAccessToken(request);

        assertTrue(result.isPresent(), "Harus menemukan token dari cookie");
        assertEquals("eyJhbGciOiJIUzUxMiJ9.test.sig", result.get());
    }

    @Test
    @DisplayName("tidak ada cookie sama sekali → Optional.empty()")
    void readAccessToken_noCookies_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // tidak set cookie apapun

        Optional<String> result = reader.readAccessToken(request);

        assertTrue(result.isEmpty(), "Harus mengembalikan empty jika tidak ada cookie");
    }

    @Test
    @DisplayName("cookie lain ada tapi bukan accessToken → Optional.empty()")
    void readAccessToken_wrongCookieName_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new javax.servlet.http.Cookie("refreshToken", "some-refresh-token")
        );

        Optional<String> result = reader.readAccessToken(request);

        assertTrue(result.isEmpty(), "Cookie dengan nama lain tidak boleh terbaca");
    }

    @Test
    @DisplayName("cookie accessToken ada tapi nilainya kosong → Optional.empty()")
    void readAccessToken_emptyCookieValue_returnsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new javax.servlet.http.Cookie(COOKIE_NAME, "   ")
        );

        Optional<String> result = reader.readAccessToken(request);

        assertTrue(result.isEmpty(), "Cookie dengan nilai kosong harus diabaikan");
    }

    @Test
    @DisplayName("banyak cookie — hanya accessToken yang dibaca")
    void readAccessToken_multipleCookies_returnsCorrectOne() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new javax.servlet.http.Cookie("sessionId", "abc123"),
                new javax.servlet.http.Cookie(COOKIE_NAME, "correct-token"),
                new javax.servlet.http.Cookie("theme", "dark")
        );

        Optional<String> result = reader.readAccessToken(request);

        assertTrue(result.isPresent());
        assertEquals("correct-token", result.get());
    }
}
