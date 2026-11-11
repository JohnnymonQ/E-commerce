package com.jquezadp.ecommerce.product.service;

import com.jquezadp.ecommerce.product.ProductCategory;
import com.jquezadp.ecommerce.product.ProductDto;
import com.jquezadp.ecommerce.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTests {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntityCopiesEveryCommandField() {
        Product product = mapper.toEntity(command("SKU-1", "Phone"));

        assertThat(product.getId()).isNull();
        assertThat(product.getVersion()).isNull();
        assertThat(product.getName()).isEqualTo("Phone");
        assertThat(product.getSku()).isEqualTo("SKU-1");
        assertThat(product.getDescription()).isEqualTo("Description");
        assertThat(product.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(product.getPrice()).isEqualByComparingTo("19.99");
        assertThat(product.getStock()).isEqualTo(5);
        assertThat(product.getWeightKg()).isEqualByComparingTo("1.250");
    }

    @Test
    void updateOverwritesBusinessFieldsAndKeepsIdentity() {
        Product product = mapper.toEntity(command("SKU-1", "Phone"));
        ReflectionTestUtils.setField(product, "id", 42L);
        ReflectionTestUtils.setField(product, "version", 3L);

        mapper.update(product, command("SKU-2", "Renamed"));

        assertThat(product.getId()).isEqualTo(42L);
        assertThat(product.getVersion()).isEqualTo(3L);
        assertThat(product.getName()).isEqualTo("Renamed");
        assertThat(product.getSku()).isEqualTo("SKU-2");
    }

    @Test
    void toDtoCopiesEveryFieldIncludingTimestamps() {
        Product product = mapper.toEntity(command("SKU-1", "Phone"));
        Instant now = Instant.now();
        ReflectionTestUtils.setField(product, "id", 7L);
        ReflectionTestUtils.setField(product, "createdAt", now);
        ReflectionTestUtils.setField(product, "updatedAt", now);

        ProductDto dto = mapper.toDto(product);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("Phone");
        assertThat(dto.sku()).isEqualTo("SKU-1");
        assertThat(dto.description()).isEqualTo("Description");
        assertThat(dto.category()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(dto.price()).isEqualByComparingTo("19.99");
        assertThat(dto.stock()).isEqualTo(5);
        assertThat(dto.weightKg()).isEqualByComparingTo("1.250");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }

    private ProductCommand command(String sku, String name) {
        return new ProductCommand(name, sku, "Description", ProductCategory.ELECTRONICS,
                new BigDecimal("19.99"), 5, new BigDecimal("1.250"));
    }
}
