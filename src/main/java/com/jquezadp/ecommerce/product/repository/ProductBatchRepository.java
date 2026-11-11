package com.jquezadp.ecommerce.product.repository;

import com.jquezadp.ecommerce.product.domain.Product;

import java.util.List;

public interface ProductBatchRepository {

    void insertAll(List<Product> products);
}
