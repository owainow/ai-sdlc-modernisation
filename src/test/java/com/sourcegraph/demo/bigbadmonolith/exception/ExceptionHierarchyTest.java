package com.sourcegraph.demo.bigbadmonolith.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T057: Tests for exception hierarchy.
 */
class ExceptionHierarchyTest {

    @Test
    void resourceNotFoundExceptionWithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        assertThat(ex.getMessage()).isEqualTo("Not found");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void resourceNotFoundExceptionWithTypeAndId() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 42L);
        assertThat(ex.getMessage()).isEqualTo("User not found with id: 42");
    }

    @Test
    void duplicateResourceExceptionWithMessage() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        assertThat(ex.getMessage()).isEqualTo("Duplicate");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void validationExceptionWithMessage() {
        ValidationException ex = new ValidationException("Invalid input");
        assertThat(ex.getMessage()).isEqualTo("Invalid input");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
