package com.sourcegraph.demo.bigbadmonolith.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T020: Characterisation tests for DateTimeUtils — formatting/parsing,
 * document thread-unsafe SimpleDateFormat, Joda dependency.
 */
class DateTimeUtilsTest {

    @Test
    void formatDateLegacyFormatsLocalDate() {
        LocalDate date = new LocalDate(2024, 6, 15);
        String formatted = DateTimeUtils.formatDateLegacy(date);
        assertThat(formatted).isEqualTo("2024-06-15");
    }

    @Test
    void formatDateLegacyReturnsEmptyForNull() {
        String formatted = DateTimeUtils.formatDateLegacy(null);
        assertThat(formatted).isEmpty();
    }

    @Test
    void formatDateTimeVerboseFormatsDateTime() {
        DateTime dateTime = new DateTime(2024, 3, 5, 9, 7, 0);
        String formatted = DateTimeUtils.formatDateTimeVerbose(dateTime);
        assertThat(formatted).isEqualTo("2024-03-05 09:07");
    }

    @Test
    void formatDateTimeVerboseThrowsForNull() {
        assertThatThrownBy(() -> DateTimeUtils.formatDateTimeVerbose(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("null");
    }

    @Test
    void convertToJavaUtilDateConvertsCorrectly() {
        DateTime dateTime = new DateTime(2024, 1, 15, 10, 30, 0);
        java.util.Date result = DateTimeUtils.convertToJavaUtilDate(dateTime);
        assertThat(result).isNotNull();
        assertThat(result.getTime()).isEqualTo(dateTime.getMillis());
    }

    @Test
    void convertToTimestampConvertsCorrectly() {
        DateTime dateTime = new DateTime(2024, 1, 15, 10, 30, 0);
        Timestamp result = DateTimeUtils.convertToTimestamp(dateTime);
        assertThat(result).isNotNull();
        assertThat(result.getTime()).isEqualTo(dateTime.getMillis());
    }

    @Test
    void convertToSqlDateConvertsCorrectly() {
        LocalDate localDate = new LocalDate(2024, 6, 15);
        Date result = DateTimeUtils.convertToSqlDate(localDate);
        assertThat(result).isNotNull();
    }

    @Test
    void isWorkingDayReturnsTrueForWeekday() {
        // 2024-06-17 is Monday
        LocalDate monday = new LocalDate(2024, 6, 17);
        assertThat(DateTimeUtils.isWorkingDay(monday)).isTrue();
    }

    @Test
    void isWorkingDayReturnsFalseForSaturday() {
        // 2024-06-15 is Saturday
        LocalDate saturday = new LocalDate(2024, 6, 15);
        assertThat(DateTimeUtils.isWorkingDay(saturday)).isFalse();
    }

    @Test
    void isWorkingDayReturnsFalseForSunday() {
        // 2024-06-16 is Sunday
        LocalDate sunday = new LocalDate(2024, 6, 16);
        assertThat(DateTimeUtils.isWorkingDay(sunday)).isFalse();
    }

    @Test
    void formatForDisplayUsesLegacyFormat() {
        DateTime dateTime = new DateTime(2024, 6, 15, 10, 30, 0);
        String formatted = DateTimeUtils.formatForDisplay(dateTime);
        assertThat(formatted).isEqualTo("06/15/2024");
    }

    @Test
    void getCurrentDateAndLogReturnsCurrentDate() {
        LocalDate result = DateTimeUtils.getCurrentDateAndLog();
        assertThat(result).isEqualTo(LocalDate.now());
    }

    @Test
    void documentThreadUnsafeSimpleDateFormat() {
        // Characterisation: DateTimeUtils.LEGACY_DATE_FORMAT is a static SimpleDateFormat
        // which is NOT thread-safe. The formatForDisplay method uses synchronized access
        // to work around this, but the pattern is fragile.
        // This should be migrated to java.time.DateTimeFormatter in US4.
        DateTime dateTime = new DateTime(2024, 1, 1, 0, 0, 0);
        String result = DateTimeUtils.formatForDisplay(dateTime);
        assertThat(result).isNotNull();
    }

    @Test
    void documentJodaTimeDependency() {
        // Characterisation: All date/time operations use Joda-Time (deprecated).
        // Should be migrated to java.time in US4.
        assertThat(LocalDate.now()).isInstanceOf(org.joda.time.LocalDate.class);
        assertThat(DateTime.now()).isInstanceOf(org.joda.time.DateTime.class);
    }
}
