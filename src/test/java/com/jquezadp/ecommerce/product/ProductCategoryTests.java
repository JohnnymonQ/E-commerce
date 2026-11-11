package com.jquezadp.ecommerce.product;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCategoryTests {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Footwear|FOOTWEAR",
            "electronics|ELECTRONICS",
            "Food & Beverage|FOOD",
            "Home & Office|HOME",
            "  Misc  |OTHER",
            "HOME|HOME"
    })
    void resolvesLabels(String label, ProductCategory expected) {
        assertThat(ProductCategory.fromLabel(label)).contains(expected);
    }

    @Test
    void returnsEmptyForUnknownOrBlankLabels() {
        assertThat(ProductCategory.fromLabel("Spaceships")).isEmpty();
        assertThat(ProductCategory.fromLabel(null)).isEmpty();
    }
}
