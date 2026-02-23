package com.sourcegraph.demo.bigbadmonolith;

import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * Test data factory with builder methods for creating valid entity instances
 * with sensible defaults. Simplifies test setup.
 */
public class TestDataFactory {

    public static User createUser() {
        return new User("test@example.com", "Test User");
    }

    public static User createUser(String email, String name) {
        return new User(email, name);
    }

    public static User createUserWithId(Long id, String email, String name) {
        return new User(id, email, name);
    }

    public static Customer createCustomer() {
        return new Customer("Test Customer", "customer@test.com", "123 Test St");
    }

    public static Customer createCustomer(String name, String email, String address) {
        return new Customer(name, email, address);
    }

    public static Customer createCustomerWithId(Long id, String name, String email, String address) {
        return new Customer(id, name, email, address, DateTime.now());
    }

    public static BillingCategory createBillingCategory() {
        return new BillingCategory("Development", "Software development", new BigDecimal("150.00"));
    }

    public static BillingCategory createBillingCategory(String name, String description, BigDecimal hourlyRate) {
        return new BillingCategory(name, description, hourlyRate);
    }

    public static BillingCategory createBillingCategoryWithId(Long id, String name, String description, BigDecimal hourlyRate) {
        return new BillingCategory(id, name, description, hourlyRate);
    }

    public static BillableHour createBillableHour(Long customerId, Long userId, Long categoryId) {
        return new BillableHour(customerId, userId, categoryId,
                new BigDecimal("8.00"), "Test work", LocalDate.now());
    }

    public static BillableHour createBillableHour(Long customerId, Long userId, Long categoryId,
                                                   BigDecimal hours, String note, LocalDate dateLogged) {
        return new BillableHour(customerId, userId, categoryId, hours, note, dateLogged);
    }
}
