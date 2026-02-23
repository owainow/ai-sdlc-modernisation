package com.sourcegraph.demo.bigbadmonolith.entity;

import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T009: Characterisation tests for Customer entity — constructor, getters, setters,
 * java.time.Instant createdAt field.
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
        Instant createdAt = Instant.parse("2024-01-15T10:30:00Z");
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
        Instant now = Instant.now();

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
    void createdAtUsesJavaTimeInstant() {
        Customer customer = new Customer("Test", "test@test.com", "addr");
        assertThat(customer.getCreatedAt()).isInstanceOf(Instant.class);
    }
}
