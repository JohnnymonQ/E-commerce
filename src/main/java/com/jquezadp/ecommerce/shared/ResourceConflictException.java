package com.jquezadp.ecommerce.shared;

/**
 * Base type for "state conflict" errors (e.g. duplicates), mapped to HTTP 409.
 */
public abstract class ResourceConflictException extends RuntimeException {

    protected ResourceConflictException(String message) {
        super(message);
    }
}
