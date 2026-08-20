package com.sciencebot.pos.suppliers.internal.repositories;

import com.sciencebot.pos.suppliers.internal.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    boolean existsByTaxId(String taxId);
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.taxId) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> searchSuppliers(@Param("search") String search, Pageable pageable);
}
