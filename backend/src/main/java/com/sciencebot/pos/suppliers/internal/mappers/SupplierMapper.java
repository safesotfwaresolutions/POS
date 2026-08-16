package com.sciencebot.pos.suppliers.internal.mappers;

import com.sciencebot.pos.suppliers.SupplierDto;
import com.sciencebot.pos.suppliers.internal.entities.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierDto toDto(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        return new SupplierDto(
                supplier.getId(),
                supplier.getCompanyName(),
                supplier.getTaxId(),
                supplier.getContactName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.isActive()
        );
    }
}
