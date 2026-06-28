package com.jtk.ps.api.unit;

import com.jtk.ps.api.util.DateUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BUG-011 — Unit test untuk DateUtil (management-content-service).
 * Memastikan catch swallow sudah diganti dengan log.warn + return null
 * sehingga caller tetap mendapat null (bukan exception tak terduga).
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
    @DisplayName("stringToDate() - input format salah harus mengembalikan null (bukan exception)")
    void stringToDate_invalidFormat_returnsNull() {
        Date result = DateUtil.stringToDate("14-06-2024");
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
    @DisplayName("stringToDate() - input bukan tanggal ('abc') harus mengembalikan null")
    void stringToDate_randomString_returnsNull() {
        Date result = DateUtil.stringToDate("abc/xyz/!!");
        assertNull(result, "String bukan tanggal harus menghasilkan null");
    }

    // ──────────────────────────────────────────────────────────────
    // parseDateToString()
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("parseDateToString() - hasil round-trip dengan stringToDate harus konsisten")
    void parseDateToString_roundTrip_consistent() {
        String input = "2024/03/15";
        Date date = DateUtil.stringToDate(input);
        assertNotNull(date);
        String output = DateUtil.parseDateToString(date);
        assertEquals(input, output, "Round-trip parse→format harus menghasilkan string yang sama");
    }
}
