package com.jquezadp.ecommerce.product.web;

import com.jquezadp.ecommerce.product.DuplicateSkuException;
import com.jquezadp.ecommerce.product.ProductCategory;
import com.jquezadp.ecommerce.product.ProductDto;
import com.jquezadp.ecommerce.product.ProductNotFoundException;
import com.jquezadp.ecommerce.product.service.ProductCommand;
import com.jquezadp.ecommerce.product.service.ProductService;
import com.jquezadp.ecommerce.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTests {

    private static final String VALID_BODY = """
            {"name": "Phone", "sku": "SKU-1", "description": "A phone", "category": "ELECTRONICS",
             "price": 99.90, "stock": 3, "weightKg": 0.2}
            """;

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void createReturns201WithBody() {
        when(productService.create(any(ProductCommand.class))).thenReturn(dto(7L));

        assertThat(mvc.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.id").isEqualTo(7);
    }

    @Test
    void createReturns400WithFieldErrorsForInvalidBody() {
        String invalid = """
                {"name": "", "sku": "SKU-1", "category": "ELECTRONICS", "price": 10, "stock": -1}
                """;

        assertThat(mvc.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.errors.name", path -> path.assertThat().isNotNull())
                .hasPathSatisfying("$.errors.stock", path -> path.assertThat().isNotNull());
    }

    @Test
    void createReturns409OnDuplicateSku() {
        when(productService.create(any(ProductCommand.class))).thenThrow(new DuplicateSkuException("SKU-1"));

        assertThat(mvc.post().uri("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void getReturns404ProblemDetailWhenMissing() {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        assertThat(mvc.get().uri("/api/products/99"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.detail").isEqualTo("Product not found with id 99");
    }

    @Test
    void getBySkuReturnsProduct() {
        when(productService.findBySku("SKU-1")).thenReturn(dto(1L));

        assertThat(mvc.get().uri("/api/products/sku/SKU-1"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.id").isEqualTo(1);
    }

    @Test
    void updateReturns200() {
        when(productService.update(eq(1L), any(ProductCommand.class))).thenReturn(dto(1L));

        assertThat(mvc.put().uri("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .hasStatusOk();
    }

    @Test
    void deleteReturns204() {
        assertThat(mvc.delete().uri("/api/products/1")).hasStatus(HttpStatus.NO_CONTENT);

        verify(productService).delete(1L);
    }

    @Test
    void listReturnsPagedProducts() {
        when(productService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto(1L), dto(2L))));

        assertThat(mvc.get().uri("/api/products?page=0&size=10"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.content.length()", path -> path.assertThat().isEqualTo(2))
                .hasPathSatisfying("$.page.totalElements", path -> path.assertThat().isEqualTo(2));
    }

    private static ProductDto dto(Long id) {
        return new ProductDto(id, "Phone", "SKU-1", "A phone", ProductCategory.ELECTRONICS,
                new BigDecimal("99.90"), 3, new BigDecimal("0.200"), Instant.now(), Instant.now());
    }
}
