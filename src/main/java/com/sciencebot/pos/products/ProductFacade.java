package com.sciencebot.pos.products;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ProductFacade {
    Optional<ProductDto> getById(Long id);
    ProductDto createProduct(CreateProductCommand command);
    ProductDto updateProduct(Long id, UpdateProductCommand command);
    void changeStatus(Long id, boolean active);
    void deleteProduct(Long id);
    Page<ProductDto> searchProducts(String search, Long categoryId, Boolean active, Pageable pageable);
    void updateStock(Long id, int quantityDelta);
    void updatePurchasePrice(Long id, java.math.BigDecimal newPrice);
}
