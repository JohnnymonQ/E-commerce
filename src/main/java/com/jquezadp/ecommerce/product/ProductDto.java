package com.jquezadp.ecommerce.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductDto(
        Long id,
        String name,
        String sku,
        String description,
        ProductCategory category,
        BigDecimal price,
        Integer stock,
        BigDecimal weightKg,
        Instant createdAt,
        Instant updatedAt
) {
}
