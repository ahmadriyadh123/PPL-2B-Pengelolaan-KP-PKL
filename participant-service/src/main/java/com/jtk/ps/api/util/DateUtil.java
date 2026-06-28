package com.jtk.ps.api.util;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtil {

    private DateUtil(){throw new IllegalStateException("Utility class");}

    public static String parseDateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }

    public static Date stringToDate(String date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            return dateFormat.parse(date);
        } catch (Exception e) {
            log.warn("[DateUtil] stringToDate() gagal memparse tanggal '{}': {}", date, e.getMessage());
            return null;
        }
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
