package com.sciencebot.pos.suppliers.internal.services;

import com.sciencebot.pos.suppliers.*;
import com.sciencebot.pos.suppliers.internal.entities.Supplier;
import com.sciencebot.pos.suppliers.internal.repositories.SupplierRepository;
import com.sciencebot.pos.suppliers.internal.mappers.SupplierMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierFacade {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public Optional<SupplierDto> getById(Long id) {
        return supplierRepository.findById(id).map(supplierMapper::toDto);
    }

    @Override
    @Transactional
    public SupplierDto createSupplier(CreateSupplierCommand command) {
        if (command.companyName() == null || command.companyName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        if (command.taxId() == null || command.taxId().isBlank()) {
            throw new IllegalArgumentException("El identificador fiscal (Tax ID) es obligatorio");
        }

        String cleanTaxId = command.taxId().trim();
        if (supplierRepository.existsByTaxId(cleanTaxId)) {
            throw new IllegalArgumentException("Ya existe un proveedor con el Tax ID: " + cleanTaxId);
        }

        Supplier supplier = new Supplier();
        supplier.setCompanyName(command.companyName().trim());
        supplier.setTaxId(cleanTaxId);
        supplier.setContactName(command.contactName() != null ? command.contactName().trim() : null);
        supplier.setEmail(command.email() != null ? command.email().trim() : null);
        supplier.setPhone(command.phone() != null ? command.phone().trim() : null);
        supplier.setAddress(command.address() != null ? command.address().trim() : null);
        supplier.setActive(true);

        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SupplierDto updateSupplier(Long id, UpdateSupplierCommand command) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + id));

        if (command.companyName() == null || command.companyName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }

        supplier.setCompanyName(command.companyName().trim());
        supplier.setContactName(command.contactName() != null ? command.contactName().trim() : null);
        supplier.setEmail(command.email() != null ? command.email().trim() : null);
        supplier.setPhone(command.phone() != null ? command.phone().trim() : null);
        supplier.setAddress(command.address() != null ? command.address().trim() : null);

        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + id));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    @Override
    public Page<SupplierDto> searchSuppliers(String search, Pageable pageable) {
        String cleanSearch = (search == null || search.isBlank()) ? null : search.trim();
        if (cleanSearch == null) {
            return supplierRepository.findAll(pageable).map(supplierMapper::toDto);
        }
        return supplierRepository.searchSuppliers(cleanSearch, pageable).map(supplierMapper::toDto);
    }
}
