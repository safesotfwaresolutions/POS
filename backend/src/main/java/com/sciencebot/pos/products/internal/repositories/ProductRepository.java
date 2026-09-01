package com.sciencebot.pos.products.internal.repositories;

import com.sciencebot.pos.products.internal.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByStoreIdAndInternalCode(Long storeId, String internalCode);
    Optional<Product> findByStoreIdAndBarcode(Long storeId, String barcode);

    boolean existsByStoreIdAndInternalCode(Long storeId, String internalCode);
    boolean existsByStoreIdAndBarcode(Long storeId, String barcode);

    // Nota: intencionalmente NO se filtran por local. Las categorias son globales (solo
    // SUPER_ADMIN las administra) y este chequeo se dispara al borrar una categoria global,
    // por lo que debe contar productos con esa categoria en TODOS los locales.
    boolean existsByCategoryIdAndActiveTrue(Long categoryId);
    int countByCategoryIdAndActiveTrue(Long categoryId);

}

