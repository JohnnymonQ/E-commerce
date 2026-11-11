package com.jquezadp.ecommerce.product.service;

import com.jquezadp.ecommerce.product.ProductCategory;
import com.jquezadp.ecommerce.product.domain.Product;
import com.jquezadp.ecommerce.product.repository.ProductRepository;
import com.jquezadp.ecommerce.shared.importing.CsvFieldException;
import com.jquezadp.ecommerce.shared.importing.CsvImporter;
import com.jquezadp.ecommerce.shared.importing.CsvRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
class ProductCsvImporter implements CsvImporter<ProductCommand> {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Set<String> requiredHeaders() {
        return Set.of("name", "sku", "description", "category", "price", "stock", "weight_kg");
    }

    @Override
    public ProductCommand map(CsvRow row) {
        return new ProductCommand(
                row.get("name"),
                row.get("sku"),
                row.get("description"),
                parseCategory(row.get("category")),
                row.getMoney("price"),
                row.getInteger("stock"),
                row.getDecimal("weight_kg"));
    }

    @Override
    public String keyField() {
        return "sku";
    }

    @Override
    public String keyOf(ProductCommand item) {
        return item.sku();
    }

    @Override
    @Transactional
    public Set<String> insertNew(List<ProductCommand> items) {
        Set<String> existing = productRepository.findExistingSkus(items.stream().map(ProductCommand::sku).toList());
        List<Product> toInsert = items.stream()
                .filter(item -> !existing.contains(item.sku()))
                .map(productMapper::toEntity)
                .toList();
        if (!toInsert.isEmpty()) {
            productRepository.insertAll(toInsert);
        }
        return existing;
    }

    private ProductCategory parseCategory(String label) {
        if (label == null) {
            return null;
        }
        return ProductCategory.fromLabel(label)
                .orElseThrow(() -> new CsvFieldException("category", "unknown category '" + label + "'"));
    }
}
