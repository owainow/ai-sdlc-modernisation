package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.TestDataFactory;
import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.integration.BaseIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * T017: Characterisation tests for BillingService — all public methods,
 * billing summary, N+1 documentation, edge cases.
 */
class BillingServiceTest extends BaseIntegrationTest {

    private final BillingService billingService = new BillingService();
    private final UserDAO userDAO = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BillingCategoryDAO categoryDAO = new BillingCategoryDAO();
    private final BillableHourDAO billableHourDAO = new BillableHourDAO();

    private User user;
    private Customer customer;
    private BillingCategory devCategory;
    private BillingCategory consultingCategory;

    @BeforeEach
    void setUpTestData() throws SQLException {
        super.cleanDatabase();
        user = userDAO.save(TestDataFactory.createUser("billing@test.com", "Billing User"));
        customer = customerDAO.save(TestDataFactory.createCustomer("Bill Corp", "bill@corp.com", "789 Bill St"));
        devCategory = categoryDAO.save(TestDataFactory.createBillingCategory("Development", "Dev", new BigDecimal("150.00")));
        consultingCategory = categoryDAO.save(TestDataFactory.createBillingCategory("Consulting", "Consult", new BigDecimal("200.00")));
    }

    @Test
    void generateCustomerBillCalculatesTotalCorrectly() throws SQLException {
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "Dev work", LocalDate.now()));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), consultingCategory.getId(),
                new BigDecimal("4.00"), "Consulting", LocalDate.now()));

        Map<String, Object> bill = billingService.generateCustomerBill(customer.getId());

        assertThat(bill).containsKey("customer");
        assertThat(bill).containsKey("totalHours");
        assertThat(bill).containsKey("totalAmount");
        assertThat(bill).containsKey("billableHours");

        BigDecimal totalHours = (BigDecimal) bill.get("totalHours");
        BigDecimal totalAmount = (BigDecimal) bill.get("totalAmount");

        // 8 * 150 + 4 * 200 = 1200 + 800 = 2000
        assertThat(totalHours).isEqualByComparingTo(new BigDecimal("12.00"));
        assertThat(totalAmount).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    void generateCustomerBillReturnsZeroForCustomerWithNoHours() throws SQLException {
        Map<String, Object> bill = billingService.generateCustomerBill(customer.getId());

        BigDecimal totalHours = (BigDecimal) bill.get("totalHours");
        BigDecimal totalAmount = (BigDecimal) bill.get("totalAmount");

        assertThat(totalHours).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totalAmount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void generateCustomerBillThrowsForNonExistentCustomer() {
        assertThatThrownBy(() -> billingService.generateCustomerBill(99999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void generateMonthlyReportFiltersByYearAndMonth() throws SQLException {
        LocalDate thisMonth = LocalDate.now();
        LocalDate lastMonth = thisMonth.minusMonths(1);

        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "This month", thisMonth));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("4.00"), "Last month", lastMonth));

        Map<String, Object> report = billingService.generateMonthlyReport(thisMonth.getYear(), thisMonth.getMonthValue());

        BigDecimal totalHours = (BigDecimal) report.get("totalHours");
        assertThat(totalHours).isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    void generateMonthlyReportGroupsRevenueByCategory() throws SQLException {
        LocalDate today = LocalDate.now();
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "Dev", today));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), consultingCategory.getId(),
                new BigDecimal("4.00"), "Consulting", today));

        Map<String, Object> report = billingService.generateMonthlyReport(today.getYear(), today.getMonthValue());

        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> revenueByCategory = (Map<String, BigDecimal>) report.get("revenueByCategory");

        assertThat(revenueByCategory).containsKey("Development");
        assertThat(revenueByCategory).containsKey("Consulting");
        assertThat(revenueByCategory.get("Development")).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(revenueByCategory.get("Consulting")).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void generateMonthlyReportReturnsEmptyForMonthWithNoHours() throws SQLException {
        Map<String, Object> report = billingService.generateMonthlyReport(2000, 1);

        BigDecimal totalHours = (BigDecimal) report.get("totalHours");
        BigDecimal totalRevenue = (BigDecimal) report.get("totalRevenue");

        assertThat(totalHours).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totalRevenue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void validateBillableHourReturnsEmptyForValidHour() throws SQLException {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "Valid work", LocalDate.now().minusDays(1));

        String errors = billingService.validateBillableHour(bh);

        // May contain weekend warning, but no validation errors
        assertThat(errors).doesNotContain("Invalid customer");
        assertThat(errors).doesNotContain("Invalid category");
        assertThat(errors).doesNotContain("Hours must be");
    }

    @Test
    void validateBillableHourRejectsInvalidCustomer() {
        BillableHour bh = TestDataFactory.createBillableHour(99999L, user.getId(), devCategory.getId());

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Invalid customer");
    }

    @Test
    void validateBillableHourRejectsInvalidCategory() {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), 99999L);

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Invalid category");
    }

    @Test
    void validateBillableHourRejectsZeroHours() {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                BigDecimal.ZERO, "Zero hours", LocalDate.now());

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Hours must be greater than zero");
    }

    @Test
    void validateBillableHourRejectsNullHours() {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                null, "Null hours", LocalDate.now());

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Hours must be greater than zero");
    }

    @Test
    void validateBillableHourRejectsNullDateLogged() {
        BillableHour bh = new BillableHour();
        bh.setCustomerId(customer.getId());
        bh.setUserId(user.getId());
        bh.setCategoryId(devCategory.getId());
        bh.setHours(new BigDecimal("8.00"));

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Date logged is required");
    }

    @Test
    void validateBillableHourRejectsFutureDate() {
        BillableHour bh = TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "Future date", LocalDate.now().plusDays(1));

        String errors = billingService.validateBillableHour(bh);
        assertThat(errors).contains("Date logged cannot be in the future");
    }

    @Test
    void documentN1QueryPattern() throws SQLException {
        // Characterisation: BillingService exhibits N+1 query pattern
        // For each BillableHour, it calls categoryDAO.findById() individually
        // instead of loading all categories in a single batch query.
        // This is documented here for performance improvement in US5.
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), devCategory.getId(),
                new BigDecimal("8.00"), "Work 1", LocalDate.now()));
        billableHourDAO.save(TestDataFactory.createBillableHour(customer.getId(), user.getId(), consultingCategory.getId(),
                new BigDecimal("4.00"), "Work 2", LocalDate.now()));

        // This generates: 1 query for hours + N queries for categories (N+1 pattern)
        Map<String, Object> bill = billingService.generateCustomerBill(customer.getId());
        assertThat(bill).isNotNull();
    }
}
