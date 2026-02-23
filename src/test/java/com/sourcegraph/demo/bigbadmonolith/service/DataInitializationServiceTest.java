package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T019: Characterisation tests for DataInitializationService — seed data, idempotency.
 */
class DataInitializationServiceTest extends BaseIntegrationTest {

    private final DataInitializationService dataInitService = new DataInitializationService();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    private final BillableHourDAO billableHourDAO = new BillableHourDAO();

    @Test
    void initializeSampleDataCreatesUsers() throws SQLException {
        dataInitService.initializeSampleData();

        List<User> users = userDAO.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@example.com");
    }

    @Test
    void initializeSampleDataCreatesCustomers() throws SQLException {
        dataInitService.initializeSampleData();

        List<Customer> customers = customerDAO.findAll();
        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::getName)
                .containsExactlyInAnyOrder("Acme Corp", "TechStart Inc", "MegaCorp Ltd");
    }

    @Test
    void initializeSampleDataCreatesBillingCategories() throws SQLException {
        dataInitService.initializeSampleData();

        List<BillingCategory> categories = categoryDAO.findAll();
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(BillingCategory::getName)
                .containsExactlyInAnyOrder("Development", "Consulting", "Support");
    }

    @Test
    void initializeSampleDataCreatesBillableHours() throws SQLException {
        dataInitService.initializeSampleData();

        List<BillableHour> hours = billableHourDAO.findAll();
        assertThat(hours).hasSize(6);
    }

    @Test
    void initializeSampleDataIsIdempotent() throws SQLException {
        dataInitService.initializeSampleData();
        dataInitService.initializeSampleData();

        // Second call should be no-op because users already exist
        List<User> users = userDAO.findAll();
        assertThat(users).hasSize(2);
    }
}
