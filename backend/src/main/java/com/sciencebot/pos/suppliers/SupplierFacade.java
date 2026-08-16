package com.sciencebot.pos.suppliers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SupplierFacade {
    Optional<SupplierDto> getById(Long id);
    SupplierDto createSupplier(CreateSupplierCommand command);
    SupplierDto updateSupplier(Long id, UpdateSupplierCommand command);
    void deleteSupplier(Long id);
    Page<SupplierDto> searchSuppliers(String search, Pageable pageable);
}
