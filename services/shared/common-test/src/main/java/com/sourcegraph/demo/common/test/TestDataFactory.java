package com.sourcegraph.demo.common.test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Factory for creating test data instances with sensible defaults.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UUID randomId() {
        return UUID.randomUUID();
    }

    public static String randomUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String randomCustomerName() {
        return "Customer " + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String randomCategoryName() {
        return "Category " + UUID.randomUUID().toString().substring(0, 8);
    }

    public static BigDecimal defaultHourlyRate() {
        return new BigDecimal("150.00");
    }

    public static BigDecimal defaultHours() {
        return new BigDecimal("8.00");
    }

    public static LocalDate defaultWorkDate() {
        return LocalDate.now().minusDays(1);
    }

    public static Instant now() {
        return Instant.now();
    }
}
