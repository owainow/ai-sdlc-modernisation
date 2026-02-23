package com.sourcegraph.demo.bigbadmonolith.exception;

/**
 * Thrown when attempting to create a resource that conflicts with an existing one.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
