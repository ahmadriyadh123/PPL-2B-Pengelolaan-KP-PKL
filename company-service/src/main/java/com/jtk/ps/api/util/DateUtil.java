package com.jtk.ps.api.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateUtil {
    private DateUtil(){throw new IllegalStateException("Utility class");}

    private static String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};

    public static String parseDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }

    public static String getDateIdNow() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        String monthString = months[month];
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        return day + " " + monthString + " " + year;
    }

    public static String parseDateToStringIdNow(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int month = cal.get(Calendar.MONTH);
        String monthString = months[month];
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int year = cal.get(Calendar.YEAR);
        return day + " " + monthString + " " + year;
    }

public static Date stringToDate(String date) {
    if (date == null || date.trim().isEmpty()) {
        return null;
    }
    
    // Coba beberapa format yang mungkin dikirim frontend
    String[] formats = {
        "yyyy-MM-dd",   // format browser date picker (HTML standard)
        "yyyy/MM/dd",   // format lama
        "dd-MM-yyyy",   // format alternatif
        "dd/MM/yyyy"    // format alternatif
    };
    
    Exception lastException = null;
    for (String format : formats) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setLenient(false); // strict parsing
            return dateFormat.parse(date);
        } catch (Exception e) {
            // coba format berikutnya
            lastException = e;
        }
    }
    
    log.warn("[DateUtil] stringToDate() gagal memparse tanggal '{}': {}", date, lastException != null ? lastException.getMessage() : "Unknown error");
    return null; // semua format gagal
}

    public static Boolean checkNowDate(String startDate, String endDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date nowDate = new Date();
            Date dateStart = dateFormat.parse(startDate);
            Date dateEnd = dateFormat.parse(endDate);

            return nowDate.after(dateStart) && nowDate.before(dateEnd);
        } catch (Exception e) {
            log.warn("[DateUtil] checkNowDate() gagal memparse rentang tanggal start='{}' end='{}': {}", startDate, endDate, e.getMessage());
            return null;
        }
    }
}
