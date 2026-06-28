package com.jtk.ps.api.unit;

import com.jtk.ps.api.util.DateUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BUG-011 — Unit test untuk DateUtil (company-service).
 * Memastikan catch swallow sudah diganti dengan log.warn + return null
 * pada stringToDate() dan checkNowDate().
 */
class DateUtilTest {

    // ──────────────────────────────────────────────────────────────
    // stringToDate()
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stringToDate() - input valid harus menghasilkan objek Date")
    void stringToDate_validInput_returnsDate() {
        Date result = DateUtil.stringToDate("2024/06/14");
        assertNotNull(result, "Tanggal valid harus berhasil di-parse menjadi Date");
    }

    @Test
    @DisplayName("stringToDate() - format salah harus mengembalikan null (bukan exception)")
    void stringToDate_invalidFormat_returnsNull() {
        Date result = DateUtil.stringToDate("14.06.2024");
        assertNull(result, "Format tanggal salah harus menghasilkan null, bukan exception");
    }

    @Test
    @DisplayName("stringToDate() - input string kosong harus mengembalikan null")
    void stringToDate_emptyString_returnsNull() {
        Date result = DateUtil.stringToDate("");
        assertNull(result, "String kosong harus menghasilkan null");
    }

    @Test
    @DisplayName("stringToDate() - input null harus mengembalikan null")
    void stringToDate_nullInput_returnsNull() {
        Date result = DateUtil.stringToDate(null);
        assertNull(result, "Input null harus menghasilkan null");
    }

    @Test
    @DisplayName("stringToDate() - string bukan tanggal harus mengembalikan null")
    void stringToDate_randomString_returnsNull() {
        Date result = DateUtil.stringToDate("bukan/tanggal/ini");
        assertNull(result, "String bukan tanggal harus menghasilkan null");
    }

    // ──────────────────────────────────────────────────────────────
    // checkNowDate()
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkNowDate() - tanggal valid yang mencakup 'sekarang' harus mengembalikan true")
    void checkNowDate_nowWithinRange_returnsTrue() {
        // Rentang sangat lebar — hari ini pasti ada di dalamnya
        Boolean result = DateUtil.checkNowDate("2000/01/01", "2099/12/31");
        assertNotNull(result, "Tidak boleh null untuk rentang tanggal valid");
        assertTrue(result, "Tanggal sekarang harus berada dalam rentang 2000–2099");
    }

    @Test
    @DisplayName("checkNowDate() - tanggal valid yang sudah lewat harus mengembalikan false")
    void checkNowDate_nowOutsideRange_returnsFalse() {
        // Rentang sudah lewat sepenuhnya
        Boolean result = DateUtil.checkNowDate("2000/01/01", "2001/01/01");
        assertNotNull(result, "Tidak boleh null untuk rentang tanggal valid");
        assertFalse(result, "Tanggal sekarang harus berada di luar rentang 2000–2001");
    }

    @Test
    @DisplayName("checkNowDate() - startDate format salah harus mengembalikan null (bukan exception)")
    void checkNowDate_invalidStartDate_returnsNull() {
        Boolean result = DateUtil.checkNowDate("INVALID_START", "2099/12/31");
        assertNull(result, "Format startDate salah harus menghasilkan null, bukan exception");
    }

    @Test
    @DisplayName("checkNowDate() - endDate format salah harus mengembalikan null (bukan exception)")
    void checkNowDate_invalidEndDate_returnsNull() {
        Boolean result = DateUtil.checkNowDate("2000/01/01", "INVALID_END");
        assertNull(result, "Format endDate salah harus menghasilkan null, bukan exception");
    }

    @Test
    @DisplayName("checkNowDate() - kedua parameter null harus mengembalikan null")
    void checkNowDate_nullInputs_returnsNull() {
        Boolean result = DateUtil.checkNowDate(null, null);
        assertNull(result, "Input null harus menghasilkan null");
    }

    // ──────────────────────────────────────────────────────────────
    // parseDateToString()
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseDateToString() - round-trip konsisten dengan stringToDate()")
    void parseDateToString_roundTrip_consistent() {
        String input = "2024/06/14";
        Date date = DateUtil.stringToDate(input);
        assertNotNull(date);
        String output = DateUtil.parseDateToString(date);
        assertEquals(input, output, "Round-trip parse→format harus menghasilkan string yang sama");
    }
}
