package com.jquezadp.ecommerce.product;

import com.jquezadp.ecommerce.shared.ResourceConflictException;

public class DuplicateSkuException extends ResourceConflictException {

    public DuplicateSkuException(String sku) {
        super("A product with sku " + sku + " already exists");
    }
}
