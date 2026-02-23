package com.sourcegraph.demo.bigbadmonolith.integration;

import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.service.DataInitializationService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T022: Integration test for startup initialisation —
 * DataInitializationService → verify seed data.
 */
class StartupInitialisationIntegrationTest extends BaseIntegrationTest {

    private final DataInitializationService dataInitService = new DataInitializationService();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    private final BillableHourDAO billableHourDAO = new BillableHourDAO();

    @Test
    void initializationCreatesExpectedSeedData() throws SQLException {
        dataInitService.initializeSampleData();

        // Verify 2 users
        List<User> users = userDAO.findAll();
        assertThat(users).hasSize(2);

        User john = userDAO.findByEmail("john.doe@example.com");
        assertThat(john).isNotNull();
        assertThat(john.getName()).isEqualTo("John Doe");

        User jane = userDAO.findByEmail("jane.smith@example.com");
        assertThat(jane).isNotNull();
        assertThat(jane.getName()).isEqualTo("Jane Smith");

        // Verify 3 customers
        List<Customer> customers = customerDAO.findAll();
        assertThat(customers).hasSize(3);
        assertThat(customers).extracting(Customer::getName)
                .containsExactlyInAnyOrder("Acme Corp", "TechStart Inc", "MegaCorp Ltd");

        // Verify 3 billing categories with expected rates
        List<BillingCategory> categories = categoryDAO.findAll();
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(BillingCategory::getName)
                .containsExactlyInAnyOrder("Development", "Consulting", "Support");

        // Verify 6 billable hours
        List<BillableHour> hours = billableHourDAO.findAll();
        assertThat(hours).hasSize(6);
    }

    @Test
    void initializationIsIdempotent() throws SQLException {
        dataInitService.initializeSampleData();
        dataInitService.initializeSampleData();
        dataInitService.initializeSampleData();

        // Only 2 users should exist regardless of how many times init is called
        assertThat(userDAO.findAll()).hasSize(2);
        assertThat(customerDAO.findAll()).hasSize(3);
        assertThat(categoryDAO.findAll()).hasSize(3);
        assertThat(billableHourDAO.findAll()).hasSize(6);
    }

    @Test
    void seedDataReferencesAreConsistent() throws SQLException {
        dataInitService.initializeSampleData();

        // All billable hours should reference valid users, customers, and categories
        List<BillableHour> hours = billableHourDAO.findAll();
        for (BillableHour hour : hours) {
            assertThat(hour.getUserId()).isNotNull();
            assertThat(hour.getCustomerId()).isNotNull();
            assertThat(hour.getCategoryId()).isNotNull();
            assertThat(hour.getHours()).isNotNull();
            assertThat(hour.getDateLogged()).isNotNull();

            // Verify FK references exist
            assertThat(userDAO.findById(hour.getUserId())).isNotNull();
            assertThat(customerDAO.findById(hour.getCustomerId())).isNotNull();
            assertThat(categoryDAO.findById(hour.getCategoryId())).isNotNull();
        }
    }
}
