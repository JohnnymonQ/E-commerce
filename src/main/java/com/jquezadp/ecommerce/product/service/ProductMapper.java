package com.jquezadp.ecommerce.product.service;

import com.jquezadp.ecommerce.product.ProductDto;
import com.jquezadp.ecommerce.product.domain.Product;
import org.springframework.stereotype.Component;

@Component
class ProductMapper {

    ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getWeightKg(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    Product toEntity(ProductCommand command) {
        return new Product(
                command.name(),
                command.sku(),
                command.description(),
                command.category(),
                command.price(),
                command.stock(),
                command.weightKg());
    }

    void update(Product product, ProductCommand command) {
        product.setName(command.name());
        product.setSku(command.sku());
        product.setDescription(command.description());
        product.setCategory(command.category());
        product.setPrice(command.price());
        product.setStock(command.stock());
        product.setWeightKg(command.weightKg());
    }
}
