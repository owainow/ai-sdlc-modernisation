package com.sourcegraph.demo.bigbadmonolith.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T008: Characterisation tests for User entity — constructor, getters, setters.
 */
class UserTest {

    @Test
    void defaultConstructorCreatesEmptyUser() {
        User user = new User();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getName()).isNull();
    }

    @Test
    void twoArgConstructorSetsEmailAndName() {
        User user = new User("john@example.com", "John Doe");
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    void threeArgConstructorSetsAllFields() {
        User user = new User(1L, "john@example.com", "John Doe");
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getName()).isEqualTo("John Doe");
    }

    @Test
    void settersUpdateFields() {
        User user = new User();
        user.setId(42L);
        user.setEmail("updated@example.com");
        user.setName("Updated Name");

        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getName()).isEqualTo("Updated Name");
    }

    @Test
    void nullValuesAreAccepted() {
        User user = new User(null, null, null);
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getName()).isNull();
    }
}
