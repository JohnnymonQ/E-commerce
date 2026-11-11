package com.jquezadp.ecommerce.product.repository;

import com.jquezadp.ecommerce.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
class ProductBatchRepositoryImpl implements ProductBatchRepository {

    private static final String INSERT_SQL = """
            INSERT INTO products (name, sku, description, category, price, stock, weight_kg,
                                  version, deleted, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0, FALSE, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertAll(List<Product> products) {
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.batchUpdate(INSERT_SQL, products, products.size(), (statement, product) -> {
            statement.setString(1, product.getName());
            statement.setString(2, product.getSku());
            statement.setString(3, product.getDescription());
            statement.setString(4, product.getCategory().name());
            statement.setBigDecimal(5, product.getPrice());
            statement.setInt(6, product.getStock());
            statement.setBigDecimal(7, product.getWeightKg());
            statement.setTimestamp(8, now);
            statement.setTimestamp(9, now);
        });
    }
}
