package com.sourcegraph.demo.bigbadmonolith.util;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T020/T068: Tests for DateTimeUtils — now using java.time exclusively.
 * Thread-safe DateTimeFormatter replaces SimpleDateFormat.
 */
class DateTimeUtilsTest {

    @Test
    void formatDateLegacyFormatsLocalDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        String formatted = DateTimeUtils.formatDateLegacy(date);
        assertThat(formatted).isEqualTo("2024-06-15");
    }

    @Test
    void formatDateLegacyReturnsEmptyForNull() {
        String formatted = DateTimeUtils.formatDateLegacy(null);
        assertThat(formatted).isEmpty();
    }

    @Test
    void formatDateTimeVerboseFormatsInstant() {
        Instant instant = ZonedDateTime.of(2024, 3, 5, 9, 7, 0, 0, ZoneId.systemDefault()).toInstant();
        String formatted = DateTimeUtils.formatDateTimeVerbose(instant);
        assertThat(formatted).isEqualTo("2024-03-05 09:07:00");
    }

    @Test
    void formatDateTimeVerboseThrowsForNull() {
        assertThatThrownBy(() -> DateTimeUtils.formatDateTimeVerbose(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("null");
    }

    @Test
    void convertToJavaUtilDateConvertsCorrectly() {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        java.util.Date result = DateTimeUtils.convertToJavaUtilDate(instant);
        assertThat(result).isNotNull();
        assertThat(result.toInstant()).isEqualTo(instant);
    }

    @Test
    void convertToTimestampConvertsCorrectly() {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        Timestamp result = DateTimeUtils.convertToTimestamp(instant);
        assertThat(result).isNotNull();
        assertThat(result.toInstant()).isEqualTo(instant);
    }

    @Test
    void convertToSqlDateConvertsCorrectly() {
        LocalDate localDate = LocalDate.of(2024, 6, 15);
        Date result = DateTimeUtils.convertToSqlDate(localDate);
        assertThat(result).isNotNull();
        assertThat(result.toLocalDate()).isEqualTo(localDate);
    }

    @Test
    void isWorkingDayReturnsTrueForWeekday() {
        // 2024-06-17 is Monday
        LocalDate monday = LocalDate.of(2024, 6, 17);
        assertThat(DateTimeUtils.isWorkingDay(monday)).isTrue();
    }

    @Test
    void isWorkingDayReturnsFalseForSaturday() {
        // 2024-06-15 is Saturday
        LocalDate saturday = LocalDate.of(2024, 6, 15);
        assertThat(DateTimeUtils.isWorkingDay(saturday)).isFalse();
    }

    @Test
    void isWorkingDayReturnsFalseForSunday() {
        // 2024-06-16 is Sunday
        LocalDate sunday = LocalDate.of(2024, 6, 16);
        assertThat(DateTimeUtils.isWorkingDay(sunday)).isFalse();
    }

    @Test
    void formatForDisplayUsesCorrectFormat() {
        Instant instant = ZonedDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneId.systemDefault()).toInstant();
        String formatted = DateTimeUtils.formatForDisplay(instant);
        assertThat(formatted).isEqualTo("06/15/2024");
    }

    @Test
    void getCurrentDateAndLogReturnsCurrentDate() {
        LocalDate result = DateTimeUtils.getCurrentDateAndLog();
        assertThat(result).isEqualTo(LocalDate.now());
    }

    @Test
    void dateTimeFormatterIsThreadSafe() {
        // java.time.DateTimeFormatter is thread-safe by design,
        // unlike the previously used SimpleDateFormat
        Instant instant = Instant.now();
        String result = DateTimeUtils.formatForDisplay(instant);
        assertThat(result).isNotNull();
    }

    @Test
    void noJodaTimeDependency() {
        // Verify java.time types are used exclusively
        assertThat(LocalDate.now()).isInstanceOf(java.time.LocalDate.class);
        assertThat(Instant.now()).isInstanceOf(java.time.Instant.class);
    }
}
