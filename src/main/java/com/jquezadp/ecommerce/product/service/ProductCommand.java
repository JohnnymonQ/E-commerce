package com.jquezadp.ecommerce.product.service;

import com.jquezadp.ecommerce.product.ProductCategory;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated input for creating or fully replacing a product.
 */
public record ProductCommand(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 64) String sku,
        String description,
        @NotNull ProductCategory category,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer stock,
        @PositiveOrZero @Digits(integer = 5, fraction = 3) BigDecimal weightKg
) {
}
