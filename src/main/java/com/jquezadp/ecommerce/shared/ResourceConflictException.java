package com.jquezadp.ecommerce.shared;

public abstract class ResourceConflictException extends RuntimeException {

    protected ResourceConflictException(String message) {
        super(message);
    }
}
