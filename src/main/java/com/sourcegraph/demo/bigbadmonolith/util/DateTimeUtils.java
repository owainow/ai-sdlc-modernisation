package com.sourcegraph.demo.bigbadmonolith.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public static String formatDateLegacy(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }
    
    public static String formatDateTimeVerbose(Instant instant) {
        if (instant == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        return DATETIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }
    
    public static java.util.Date convertToJavaUtilDate(Instant instant) {
        return java.util.Date.from(instant);
    }
    
    public static Timestamp convertToTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }
    
    public static Date convertToSqlDate(LocalDate localDate) {
        return Date.valueOf(localDate);
    }
    
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
    
    public static String formatForDisplay(Instant instant) {
        return DISPLAY_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }
    
    public static LocalDate getCurrentDateAndLog() {
        LocalDate now = LocalDate.now();
        System.out.println("Current date requested: " + now);
        return now;
    }
}
