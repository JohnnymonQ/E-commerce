package com.jquezadp.ecommerce.product;

import com.jquezadp.ecommerce.TestcontainersConfiguration;
import com.jquezadp.ecommerce.product.repository.ProductRepository;
import com.jquezadp.ecommerce.product.service.ProductCommand;
import com.jquezadp.ecommerce.product.service.ProductService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class ProductServiceIntegrationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    @Test
    void createsAndFindsProduct() {
        ProductDto created = productService.create(command("SKU-1"));

        assertThat(created.id()).isNotNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(productService.findById(created.id()).sku()).isEqualTo("SKU-1");
        assertThat(productService.findBySku("SKU-1").id()).isEqualTo(created.id());
    }

    @Test
    void rejectsDuplicateSkuOnCreate() {
        productService.create(command("SKU-1"));

        assertThatThrownBy(() -> productService.create(command("SKU-1")))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void updatesProduct() {
        ProductDto created = productService.create(command("SKU-1"));
        ProductCommand changes = new ProductCommand("Renamed", "SKU-1", "New description",
                ProductCategory.BOOKS, new BigDecimal("19.99"), 5, null);

        ProductDto updated = productService.update(created.id(), changes);

        assertThat(updated.name()).isEqualTo("Renamed");
        assertThat(updated.category()).isEqualTo(ProductCategory.BOOKS);
        assertThat(updated.price()).isEqualByComparingTo("19.99");
        assertThat(updated.stock()).isEqualTo(5);
        assertThat(updated.weightKg()).isNull();
    }

    @Test
    void rejectsUpdateToSkuOfAnotherProduct() {
        productService.create(command("SKU-1"));
        ProductDto second = productService.create(command("SKU-2"));

        assertThatThrownBy(() -> productService.update(second.id(), command("SKU-1")))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void updateFailsWhenProductDoesNotExist() {
        assertThatThrownBy(() -> productService.update(999_999L, command("SKU-1")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deletesProduct() {
        ProductDto created = productService.create(command("SKU-1"));

        productService.delete(created.id());

        assertThatThrownBy(() -> productService.findById(created.id()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void pagesProducts() {
        productService.create(command("SKU-1"));
        productService.create(command("SKU-2"));
        productService.create(command("SKU-3"));

        Page<ProductDto> page = productService.findAll(PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void rejectsInvalidCommand() {
        ProductCommand invalid = new ProductCommand(" ", "SKU-1", null,
                ProductCategory.HOME, new BigDecimal("10.00"), -1, null);

        assertThatThrownBy(() -> productService.create(invalid))
                .isInstanceOf(ConstraintViolationException.class);
    }

    private ProductCommand command(String sku) {
        return new ProductCommand("Product " + sku, sku, "Description", ProductCategory.ELECTRONICS,
                new BigDecimal("10.50"), 10, new BigDecimal("1.250"));
    }
}
