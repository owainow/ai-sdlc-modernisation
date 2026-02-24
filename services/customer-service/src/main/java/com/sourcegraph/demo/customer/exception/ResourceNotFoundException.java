package com.sourcegraph.demo.customer.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " with id '" + id + "' was not found.");
    }
}
