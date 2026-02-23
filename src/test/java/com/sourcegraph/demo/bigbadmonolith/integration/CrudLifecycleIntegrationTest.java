package com.sourcegraph.demo.bigbadmonolith.integration;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T021: Integration test for full CRUD lifecycle —
 * User → Customer → BillingCategory → BillableHour → verify → delete.
 */
class CrudLifecycleIntegrationTest extends BaseIntegrationTest {

    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    private final BillableHourDAO billableHourDAO = new BillableHourDAO();

    @Test
    void fullCrudLifecycle() throws SQLException {
        // CREATE
        User user = userDAO.save(TestDataFactory.createUser("lifecycle@test.com", "Lifecycle User"));
        assertThat(user.getId()).isNotNull();

        Customer customer = customerDAO.save(TestDataFactory.createCustomer("Lifecycle Corp", "lc@corp.com", "123 LC St"));
        assertThat(customer.getId()).isNotNull();

        BillingCategory category = categoryDAO.save(
                TestDataFactory.createBillingCategory("Integration", "Integration testing", new BigDecimal("175.00")));
        assertThat(category.getId()).isNotNull();

        BillableHour hour = billableHourDAO.save(
                TestDataFactory.createBillableHour(customer.getId(), user.getId(), category.getId(),
                        new BigDecimal("6.50"), "Integration test work", LocalDate.now()));
        assertThat(hour.getId()).isNotNull();

        // READ
        User foundUser = userDAO.findById(user.getId());
        assertThat(foundUser.getEmail()).isEqualTo("lifecycle@test.com");

        Customer foundCustomer = customerDAO.findById(customer.getId());
        assertThat(foundCustomer.getName()).isEqualTo("Lifecycle Corp");

        BillingCategory foundCategory = categoryDAO.findById(category.getId());
        assertThat(foundCategory.getHourlyRate()).isEqualByComparingTo(new BigDecimal("175.00"));

        BillableHour foundHour = billableHourDAO.findById(hour.getId());
        assertThat(foundHour.getHours()).isEqualByComparingTo(new BigDecimal("6.50"));

        // UPDATE
        user.setName("Updated Lifecycle User");
        userDAO.update(user);
        assertThat(userDAO.findById(user.getId()).getName()).isEqualTo("Updated Lifecycle User");

        customer.setName("Updated Corp");
        customerDAO.update(customer);
        assertThat(customerDAO.findById(customer.getId()).getName()).isEqualTo("Updated Corp");

        category.setHourlyRate(new BigDecimal("200.00"));
        categoryDAO.update(category);
        assertThat(categoryDAO.findById(category.getId()).getHourlyRate())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        hour.setHours(new BigDecimal("8.00"));
        billableHourDAO.update(hour);
        assertThat(billableHourDAO.findById(hour.getId()).getHours())
                .isEqualByComparingTo(new BigDecimal("8.00"));

        // VERIFY COUNTS
        List<User> allUsers = userDAO.findAll();
        assertThat(allUsers).hasSize(1);

        List<Customer> allCustomers = customerDAO.findAll();
        assertThat(allCustomers).hasSize(1);

        List<BillingCategory> allCategories = categoryDAO.findAll();
        assertThat(allCategories).hasSize(1);

        List<BillableHour> allHours = billableHourDAO.findAll();
        assertThat(allHours).hasSize(1);

        // DELETE (reverse order due to FK constraints)
        assertThat(billableHourDAO.delete(hour.getId())).isTrue();
        assertThat(categoryDAO.delete(category.getId())).isTrue();
        assertThat(customerDAO.delete(customer.getId())).isTrue();
        assertThat(userDAO.delete(user.getId())).isTrue();

        // VERIFY DELETION
        assertThat(userDAO.findAll()).isEmpty();
        assertThat(customerDAO.findAll()).isEmpty();
        assertThat(categoryDAO.findAll()).isEmpty();
        assertThat(billableHourDAO.findAll()).isEmpty();
    }

    @Test
    void findByRelationships() throws SQLException {
        User user1 = userDAO.save(TestDataFactory.createUser("user1@test.com", "User 1"));
        User user2 = userDAO.save(TestDataFactory.createUser("user2@test.com", "User 2"));

        Customer customer1 = customerDAO.save(TestDataFactory.createCustomer("Corp A", "a@corp.com", "A St"));
        Customer customer2 = customerDAO.save(TestDataFactory.createCustomer("Corp B", "b@corp.com", "B St"));

        BillingCategory category = categoryDAO.save(
                TestDataFactory.createBillingCategory("Dev", "Development", new BigDecimal("150.00")));

        billableHourDAO.save(TestDataFactory.createBillableHour(customer1.getId(), user1.getId(), category.getId(),
                new BigDecimal("4.00"), "User1 for Corp A", LocalDate.now()));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer1.getId(), user2.getId(), category.getId(),
                new BigDecimal("6.00"), "User2 for Corp A", LocalDate.now()));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer2.getId(), user1.getId(), category.getId(),
                new BigDecimal("3.00"), "User1 for Corp B", LocalDate.now()));

        // Find by customer
        List<BillableHour> corpAHours = billableHourDAO.findByCustomerId(customer1.getId());
        assertThat(corpAHours).hasSize(2);

        List<BillableHour> corpBHours = billableHourDAO.findByCustomerId(customer2.getId());
        assertThat(corpBHours).hasSize(1);

        // Find by user
        List<BillableHour> user1Hours = billableHourDAO.findByUserId(user1.getId());
        assertThat(user1Hours).hasSize(2);

        List<BillableHour> user2Hours = billableHourDAO.findByUserId(user2.getId());
        assertThat(user2Hours).hasSize(1);
    }
}
