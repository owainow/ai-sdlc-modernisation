package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.exception.ResourceNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.exception.ValidationException;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import com.sourcegraph.demo.bigbadmonolith.service.impl.BillingCategoryServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T044: Contract tests for BillingCategoryService — rate validation.
 */
class BillingCategoryServiceContractTest extends BaseIntegrationTest {

    private final BillingCategoryService categoryService = new BillingCategoryServiceImpl();

    @Test
    void savePersistsValidCategory() throws SQLException {
        BillingCategory cat = categoryService.save(
                TestDataFactory.createBillingCategory("Dev", "Development", new BigDecimal("150.00")));
        assertThat(cat.getId()).isNotNull();
    }

    @Test
    void saveRejectsZeroRate() {
        assertThatThrownBy(() -> categoryService.save(
                TestDataFactory.createBillingCategory("Free", "Free", BigDecimal.ZERO)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void saveRejectsNegativeRate() {
        assertThatThrownBy(() -> categoryService.save(
                TestDataFactory.createBillingCategory("Bad", "Bad", new BigDecimal("-10.00"))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void saveRejectsExcessiveRate() {
        assertThatThrownBy(() -> categoryService.save(
                TestDataFactory.createBillingCategory("Expensive", "Expensive", new BigDecimal("10001.00"))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("10,000");
    }

    @Test
    void findByIdThrowsForNonExistent() {
        assertThatThrownBy(() -> categoryService.findById(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllReturnsCategories() throws SQLException {
        categoryService.save(TestDataFactory.createBillingCategory("A", "A", new BigDecimal("100.00")));
        categoryService.save(TestDataFactory.createBillingCategory("B", "B", new BigDecimal("200.00")));
        assertThat(categoryService.findAll()).hasSize(2);
    }
}
