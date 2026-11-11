package com.jquezadp.ecommerce.product;

import java.util.Arrays;
import java.util.Optional;

public enum ProductCategory {
    ELECTRONICS,
    CLOTHING,
    HOME("Home & Office"),
    BOOKS,
    SPORTS,
    TOYS,
    BEAUTY,
    FOOD("Food & Beverage"),
    FOOTWEAR,
    ACCESSORIES,
    OUTDOORS,
    KITCHEN,
    STATIONERY,
    GAMES,
    PETS,
    HEALTH,
    TOOLS,
    GIFTS,
    OTHER("Misc");

    private final String alias;

    ProductCategory() {
        this(null);
    }

    ProductCategory(String alias) {
        this.alias = alias;
    }

    public static Optional<ProductCategory> fromLabel(String label) {
        if (label == null) {
            return Optional.empty();
        }
        String normalized = normalize(label);
        return Arrays.stream(values())
                .filter(category -> category.name().equals(normalized)
                        || (category.alias != null && normalize(category.alias).equals(normalized)))
                .findFirst();
    }

    private static String normalize(String label) {
        return label.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
    }
}
