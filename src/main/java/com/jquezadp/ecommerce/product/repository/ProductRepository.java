package com.jquezadp.ecommerce.product.repository;

import com.jquezadp.ecommerce.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductBatchRepository {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("select p.sku from Product p where p.sku in :skus")
    Set<String> findExistingSkus(@Param("skus") Collection<String> skus);
}
