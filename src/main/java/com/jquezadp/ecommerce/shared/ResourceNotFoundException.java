package com.jquezadp.ecommerce.shared;

/**
 * Base type for "resource does not exist" errors, mapped to HTTP 404.
 */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
