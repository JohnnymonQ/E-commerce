package com.jquezadp.ecommerce.product;

import com.jquezadp.ecommerce.TestcontainersConfiguration;
import com.jquezadp.ecommerce.product.service.ProductCommand;
import com.jquezadp.ecommerce.product.service.ProductService;
import com.jquezadp.ecommerce.shared.importing.ImportReport;
import com.jquezadp.ecommerce.shared.importing.InvalidImportFileException;
import com.jquezadp.ecommerce.shared.importing.RowError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@Import(TestcontainersConfiguration.class)
class ProductCsvImportIntegrationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("DELETE FROM products");
        productService.create(new ProductCommand("Existing", "EX-001", null, ProductCategory.BOOKS,
                new BigDecimal("1.00"), 1, null));
        ProductDto deleted = productService.create(new ProductCommand("Deleted", "RE-001", null,
                ProductCategory.BOOKS, new BigDecimal("1.00"), 1, null));
        productService.delete(deleted.id());
    }

    @Test
    void importsValidRowsAndReportsTheRest() throws Exception {
        ImportReport report;
        try (InputStream csv = getClass().getResourceAsStream("/csv/products.csv")) {
            report = productService.importCsv(csv);
        }

        assertThat(report.totalRows()).isEqualTo(17);
        assertThat(report.skipped()).isEqualTo(1);
        assertThat(report.created()).isEqualTo(8);
        assertThat(report.failed()).isEqualTo(9);
        Map<String, String> errorsByLine = report.errors().stream()
                .collect(java.util.stream.Collectors.toMap(error -> String.valueOf(error.line()), this::fieldNames));
        assertThat(errorsByLine).containsEntry("9", "price")
                .containsEntry("10", "stock")
                .containsEntry("11", "name")
                .containsEntry("12", "name")
                .containsEntry("13", "name")
                .containsEntry("14", "category")
                .containsEntry("15", "category")
                .containsEntry("17", "sku")
                .containsEntry("18", "sku");

        assertThat(count("SELECT COUNT(*) FROM products WHERE deleted = FALSE")).isEqualTo(9);
        assertThat(productService.findBySku("WM-042").price()).isEqualByComparingTo("29.99");
        assertThat(productService.findBySku("SD-004").weightKg()).isNull();
        assertThat(productService.findBySku("CB-010").category()).isEqualTo(ProductCategory.FOOD);
        assertThat(productService.findBySku("SD-004").category()).isEqualTo(ProductCategory.HOME);
        assertThat(productService.findBySku("CI-001").name()).isEqualTo("Comma, In Product Name");
        assertThat(productService.findBySku("SQL-001").name()).isEqualTo("Robert'); DROP TABLE products;--");
        assertThat(productService.findBySku("RE-001").name()).isEqualTo("Reborn Product");
        assertThat(productService.findBySku("EX-001").name()).isEqualTo("Existing");
        assertThat(productService.findBySku("RS-001").stock()).isEqualTo(150);
    }

    @Test
    void importingTheSameFileTwiceCreatesNothingTheSecondTime() throws Exception {
        try (InputStream csv = getClass().getResourceAsStream("/csv/products.csv")) {
            productService.importCsv(csv);
        }

        ImportReport second;
        try (InputStream csv = getClass().getResourceAsStream("/csv/products.csv")) {
            second = productService.importCsv(csv);
        }

        assertThat(second.created()).isZero();
        assertThat(count("SELECT COUNT(*) FROM products WHERE deleted = FALSE")).isEqualTo(9);
    }

    @Test
    void rejectsFileWithoutRequiredColumns() {
        InputStream csv = new ByteArrayInputStream("name,sku\nA,B\n".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> productService.importCsv(csv)).isInstanceOf(InvalidImportFileException.class);
    }

    private String fieldNames(RowError error) {
        return String.join(",", List.copyOf(error.errors().keySet()));
    }

    private Integer count(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
