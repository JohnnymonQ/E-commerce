package com.jquezadp.ecommerce.product.service;

import com.jquezadp.ecommerce.product.DuplicateSkuException;
import com.jquezadp.ecommerce.product.ProductDto;
import com.jquezadp.ecommerce.product.ProductNotFoundException;
import com.jquezadp.ecommerce.product.domain.Product;
import com.jquezadp.ecommerce.product.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDto create(@Valid ProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException(command.sku());
        }
        Product saved = productRepository.save(productMapper.toEntity(command));
        return productMapper.toDto(saved);
    }

    public ProductDto findById(Long id) {
        return productMapper.toDto(getOrThrow(id));
    }

    public ProductDto findBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Transactional
    public ProductDto update(Long id, @Valid ProductCommand command) {
        Product product = getOrThrow(id);
        if (productRepository.existsBySkuAndIdNot(command.sku(), id)) {
            throw new DuplicateSkuException(command.sku());
        }
        productMapper.update(product, command);
        return productMapper.toDto(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(getOrThrow(id));
    }

    private Product getOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
