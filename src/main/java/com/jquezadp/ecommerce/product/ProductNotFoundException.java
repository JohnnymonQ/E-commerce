package com.jquezadp.ecommerce.product;

import com.jquezadp.ecommerce.shared.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super("Product not found with id " + id);
    }

    public ProductNotFoundException(String sku) {
        super("Product not found with sku " + sku);
    }
}
