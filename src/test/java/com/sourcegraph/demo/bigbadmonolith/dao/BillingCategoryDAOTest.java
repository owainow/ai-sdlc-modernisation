package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T014: Characterisation tests for BillingCategoryDAO — CRUD against in-memory Derby.
 */
class BillingCategoryDAOTest extends BaseIntegrationTest {

    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();

    @Test
    void saveSetsGeneratedId() throws SQLException {
        BillingCategory category = TestDataFactory.createBillingCategory("Dev", "Development", new BigDecimal("150.00"));
        BillingCategory saved = categoryDAO.save(category);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dev");
        assertThat(saved.getHourlyRate()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void saveRejectsNullCategory() {
        assertThatThrownBy(() -> categoryDAO.save(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByIdReturnsCategoryWhenExists() throws SQLException {
        BillingCategory saved = categoryDAO.save(TestDataFactory.createBillingCategory("Consulting", "Business", new BigDecimal("200.00")));

        BillingCategory found = categoryDAO.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Consulting");
        assertThat(found.getDescription()).isEqualTo("Business");
        assertThat(found.getHourlyRate()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void findByIdReturnsNullWhenNotExists() throws SQLException {
        BillingCategory found = categoryDAO.findById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void findAllReturnsAllCategories() throws SQLException {
        categoryDAO.save(TestDataFactory.createBillingCategory("Cat A", "A", new BigDecimal("100.00")));
        categoryDAO.save(TestDataFactory.createBillingCategory("Cat B", "B", new BigDecimal("200.00")));

        List<BillingCategory> categories = categoryDAO.findAll();
        assertThat(categories).hasSize(2);
    }

    @Test
    void findAllReturnsEmptyListWhenNoCategories() throws SQLException {
        List<BillingCategory> categories = categoryDAO.findAll();
        assertThat(categories).isEmpty();
    }

    @Test
    void updateModifiesExistingCategory() throws SQLException {
        BillingCategory saved = categoryDAO.save(TestDataFactory.createBillingCategory("Old", "Old desc", new BigDecimal("50.00")));
        saved.setName("New");
        saved.setDescription("New desc");
        saved.setHourlyRate(new BigDecimal("75.00"));

        boolean updated = categoryDAO.update(saved);
        assertThat(updated).isTrue();

        BillingCategory found = categoryDAO.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("New");
        assertThat(found.getDescription()).isEqualTo("New desc");
        assertThat(found.getHourlyRate()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void deleteRemovesCategory() throws SQLException {
        BillingCategory saved = categoryDAO.save(TestDataFactory.createBillingCategory("Delete", "Delete", new BigDecimal("10.00")));

        boolean deleted = categoryDAO.delete(saved.getId());
        assertThat(deleted).isTrue();
        assertThat(categoryDAO.findById(saved.getId())).isNull();
    }

    @Test
    void deleteReturnsFalseForNonExistentCategory() throws SQLException {
        boolean deleted = categoryDAO.delete(99999L);
        assertThat(deleted).isFalse();
    }
}
