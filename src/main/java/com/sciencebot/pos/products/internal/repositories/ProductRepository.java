package com.sciencebot.pos.products.internal.repositories;

import com.sciencebot.pos.products.internal.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    Optional<Product> findByInternalCode(String internalCode);
    Optional<Product> findByBarcode(String barcode);
    
    boolean existsByInternalCode(String internalCode);
    boolean existsByBarcode(String barcode);
    
    boolean existsByCategoryIdAndActiveTrue(Long categoryId);
    int countByCategoryIdAndActiveTrue(Long categoryId);

    
}

