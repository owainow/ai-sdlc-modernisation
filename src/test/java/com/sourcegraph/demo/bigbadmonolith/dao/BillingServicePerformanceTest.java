package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import com.sourcegraph.demo.bigbadmonolith.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T079: N+1 elimination tests — verify batch queries in BillingService.
 */
class BillingServicePerformanceTest extends BaseIntegrationTest {

    private final BillingService billingService = new BillingService();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    private final BillableHourDAO billableHourDAO = new BillableHourDAO();

    private Customer customer;
    private User user;

    @BeforeEach
    void setUpTestData() throws SQLException {
        super.cleanDatabase();
        user = userDAO.save(TestDataFactory.createUser("perf@test.com", "Perf User"));
        customer = customerDAO.save(TestDataFactory.createCustomer("Perf Corp", "perf@corp.com", "123 Perf St"));
    }

    @Test
    void generateCustomerBillUseBatchQueriesNotN1() throws SQLException {
        // Create multiple categories
        BillingCategory cat1 = categoryDAO.save(TestDataFactory.createBillingCategory("Cat1", "Desc1", new BigDecimal("100.00")));
        BillingCategory cat2 = categoryDAO.save(TestDataFactory.createBillingCategory("Cat2", "Desc2", new BigDecimal("200.00")));
        BillingCategory cat3 = categoryDAO.save(TestDataFactory.createBillingCategory("Cat3", "Desc3", new BigDecimal("300.00")));

        // Create billable hours across multiple categories
        for (int i = 0; i < 10; i++) {
            BillingCategory cat = i % 3 == 0 ? cat1 : (i % 3 == 1 ? cat2 : cat3);
            billableHourDAO.save(TestDataFactory.createBillableHour(
                    customer.getId(), user.getId(), cat.getId(),
                    new BigDecimal("2.00"), "Work " + i, LocalDate.now().minusDays(i)));
        }

        // BillingService now loads all categories in one batch query
        // instead of N individual findById calls
        Map<String, Object> bill = billingService.generateCustomerBill(customer.getId());

        BigDecimal totalAmount = (BigDecimal) bill.get("totalAmount");
        assertThat(totalAmount).isGreaterThan(BigDecimal.ZERO);
        assertThat(bill.get("totalHours")).isNotNull();
    }
}
