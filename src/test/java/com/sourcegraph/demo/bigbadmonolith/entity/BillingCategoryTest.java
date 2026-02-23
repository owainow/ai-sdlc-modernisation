package com.sourcegraph.demo.bigbadmonolith.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T010: Characterisation tests for BillingCategory entity — constructor, getters, setters,
 * hourly rate edge cases (zero, negative).
 */
class BillingCategoryTest {

    @Test
    void defaultConstructorCreatesEmptyCategory() {
        BillingCategory category = new BillingCategory();
        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isNull();
        assertThat(category.getDescription()).isNull();
        assertThat(category.getHourlyRate()).isNull();
    }

    @Test
    void threeArgConstructorSetsFields() {
        BillingCategory category = new BillingCategory("Development", "Dev work", new BigDecimal("150.00"));
        assertThat(category.getId()).isNull();
        assertThat(category.getName()).isEqualTo("Development");
        assertThat(category.getDescription()).isEqualTo("Dev work");
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void fourArgConstructorSetsAllFields() {
        BillingCategory category = new BillingCategory(1L, "Consulting", "Business consulting", new BigDecimal("200.00"));
        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Consulting");
        assertThat(category.getDescription()).isEqualTo("Business consulting");
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void settersUpdateFields() {
        BillingCategory category = new BillingCategory();
        category.setId(5L);
        category.setName("Support");
        category.setDescription("Technical support");
        category.setHourlyRate(new BigDecimal("100.00"));

        assertThat(category.getId()).isEqualTo(5L);
        assertThat(category.getName()).isEqualTo("Support");
        assertThat(category.getDescription()).isEqualTo("Technical support");
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void zeroHourlyRateIsAccepted() {
        // Characterisation: entity does NOT validate hourly rate
        BillingCategory category = new BillingCategory("Free", "Free tier", BigDecimal.ZERO);
        assertThat(category.getHourlyRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void negativeHourlyRateIsAccepted() {
        // Characterisation: entity does NOT validate against negative rates
        BillingCategory category = new BillingCategory("Discount", "Credit", new BigDecimal("-50.00"));
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    void highPrecisionHourlyRate() {
        BillingCategory category = new BillingCategory("Precise", "High precision rate", new BigDecimal("199.99"));
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("199.99"));
    }
}
