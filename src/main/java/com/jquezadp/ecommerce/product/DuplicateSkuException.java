package com.jquezadp.ecommerce.product;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("A product with sku " + sku + " already exists");
    }
}
