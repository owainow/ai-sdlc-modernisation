package com.sourcegraph.demo.bigbadmonolith.entity;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T009: Characterisation tests for Customer entity — constructor, getters, setters,
 * Joda-Time DateTime field.
 */
class CustomerTest {

    @Test
    void defaultConstructorCreatesEmptyCustomer() {
        Customer customer = new Customer();
        assertThat(customer.getId()).isNull();
        assertThat(customer.getName()).isNull();
        assertThat(customer.getEmail()).isNull();
        assertThat(customer.getAddress()).isNull();
        assertThat(customer.getCreatedAt()).isNull();
    }

    @Test
    void threeArgConstructorSetsFieldsAndCreatedAt() {
        Customer customer = new Customer("Acme Corp", "billing@acme.com", "123 Business St");
        assertThat(customer.getId()).isNull();
        assertThat(customer.getName()).isEqualTo("Acme Corp");
        assertThat(customer.getEmail()).isEqualTo("billing@acme.com");
        assertThat(customer.getAddress()).isEqualTo("123 Business St");
        assertThat(customer.getCreatedAt()).isNotNull();
    }

    @Test
    void fiveArgConstructorSetsAllFields() {
        DateTime createdAt = new DateTime(2024, 1, 15, 10, 30, 0);
        Customer customer = new Customer(1L, "Acme Corp", "billing@acme.com", "123 Business St", createdAt);
        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getName()).isEqualTo("Acme Corp");
        assertThat(customer.getEmail()).isEqualTo("billing@acme.com");
        assertThat(customer.getAddress()).isEqualTo("123 Business St");
        assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void settersUpdateAllFields() {
        Customer customer = new Customer();
        DateTime now = DateTime.now();

        customer.setId(42L);
        customer.setName("Updated Corp");
        customer.setEmail("updated@corp.com");
        customer.setAddress("456 New St");
        customer.setCreatedAt(now);

        assertThat(customer.getId()).isEqualTo(42L);
        assertThat(customer.getName()).isEqualTo("Updated Corp");
        assertThat(customer.getEmail()).isEqualTo("updated@corp.com");
        assertThat(customer.getAddress()).isEqualTo("456 New St");
        assertThat(customer.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void createdAtUsesJodaDateTime() {
        Customer customer = new Customer("Test", "test@test.com", "addr");
        // Characterisation: createdAt is Joda DateTime, not java.time
        assertThat(customer.getCreatedAt()).isInstanceOf(DateTime.class);
    }
}
