package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T015: Characterisation tests for BillableHourDAO — CRUD + findByUser/findByCustomer
 * against in-memory Derby.
 */
class BillableHourDAOTest extends BaseIntegrationTest {

    private final BillableHourDAO billableHourDAO = new BillableHourDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();

    private User user;
    private Customer customer;
    private BillingCategory category;

    @BeforeEach
    void setUpTestData() throws SQLException {
        super.cleanDatabase();
        user = userDAO.save(TestDataFactory.createUser("test@test.com", "Test User"));
        customer = customerDAO.save(TestDataFactory.createCustomer("Test Corp", "test@corp.com", "123 St"));
        category = categoryDAO.save(TestDataFactory.createBillingCategory("Dev", "Development", new BigDecimal("150.00")));
    }

    @Test
    void saveSetsGeneratedId() throws SQLException {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId());
        BillableHour saved = billableHourDAO.save(bh);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo(customer.getId());
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void findByIdReturnsBillableHourWhenExists() throws SQLException {
        BillableHour saved = billableHourDAO.save(
                TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId(),
                        new BigDecimal("4.50"), "Test note", LocalDate.now()));

        BillableHour found = billableHourDAO.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getHours()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(found.getNote()).isEqualTo("Test note");
    }

    @Test
    void findByIdReturnsNullWhenNotExists() throws SQLException {
        BillableHour found = billableHourDAO.findById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void findByCustomerIdReturnsMatchingHours() throws SQLException {
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId()));

        Customer customer2 = customerDAO.save(TestDataFactory.createCustomer("Other Corp", "other@corp.com", "456 St"));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer2.getId(), user.getId(), category.getId()));

        List<BillableHour> hours = billableHourDAO.findByCustomerId(customer.getId());
        assertThat(hours).hasSize(1);
        assertThat(hours.get(0).getCustomerId()).isEqualTo(customer.getId());
    }

    @Test
    void findByUserIdReturnsMatchingHours() throws SQLException {
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId()));

        User user2 = userDAO.save(TestDataFactory.createUser("other@test.com", "Other User"));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user2.getId(), category.getId()));

        List<BillableHour> hours = billableHourDAO.findByUserId(user.getId());
        assertThat(hours).hasSize(1);
        assertThat(hours.get(0).getUserId()).isEqualTo(user.getId());
    }

    @Test
    void findAllReturnsAllHours() throws SQLException {
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId()));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId(),
                new BigDecimal("3.00"), "Second entry", LocalDate.now()));

        List<BillableHour> hours = billableHourDAO.findAll();
        assertThat(hours).hasSize(2);
    }

    @Test
    void findAllReturnsEmptyListWhenNoHours() throws SQLException {
        List<BillableHour> hours = billableHourDAO.findAll();
        assertThat(hours).isEmpty();
    }

    @Test
    void updateModifiesExistingBillableHour() throws SQLException {
        BillableHour saved = billableHourDAO.save(
                TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId(),
                        new BigDecimal("4.00"), "Original note", LocalDate.now()));

        saved.setHours(new BigDecimal("6.00"));
        saved.setNote("Updated note");
        boolean updated = billableHourDAO.update(saved);
        assertThat(updated).isTrue();

        BillableHour found = billableHourDAO.findById(saved.getId());
        assertThat(found.getHours()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(found.getNote()).isEqualTo("Updated note");
    }

    @Test
    void deleteRemovesBillableHour() throws SQLException {
        BillableHour saved = billableHourDAO.save(
                TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId()));

        boolean deleted = billableHourDAO.delete(saved.getId());
        assertThat(deleted).isTrue();
        assertThat(billableHourDAO.findById(saved.getId())).isNull();
    }

    @Test
    void deleteReturnsFalseForNonExistentHour() throws SQLException {
        boolean deleted = billableHourDAO.delete(99999L);
        assertThat(deleted).isFalse();
    }
}
