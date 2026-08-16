package com.sciencebot.pos.purchases.internal.mappers;

import com.sciencebot.pos.purchases.PurchaseDto;
import com.sciencebot.pos.purchases.PurchaseItemDto;
import com.sciencebot.pos.purchases.internal.entities.Purchase;
import com.sciencebot.pos.purchases.internal.entities.PurchaseItem;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.suppliers.SupplierFacade;
import com.sciencebot.pos.users.UserFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurchaseMapper {

    private final ProductFacade productFacade;
    private final SupplierFacade supplierFacade;
    private final UserFacade userFacade;

    public PurchaseMapper(
            @Lazy ProductFacade productFacade,
            @Lazy SupplierFacade supplierFacade,
            @Lazy UserFacade userFacade) {
        this.productFacade = productFacade;
        this.supplierFacade = supplierFacade;
        this.userFacade = userFacade;
    }

    public PurchaseDto toDto(Purchase purchase) {
        if (purchase == null) {
            return null;
        }

        String supplierName = supplierFacade.getById(purchase.getSupplierId())
                .map(s -> s.companyName())
                .orElse("Proveedor Desconocido");

        String username = "Desconocido";
        try {
            username = userFacade.getById(purchase.getUserId()).username();
        } catch (Exception e) {
            // Fallback
        }

        List<PurchaseItemDto> itemDtos = purchase.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PurchaseDto(
                purchase.getId(),
                purchase.getSupplierId(),
                supplierName,
                purchase.getInvoiceNumber(),
                purchase.getTotalAmount(),
                username,
                purchase.getCreatedAt(),
                itemDtos
        );
    }

    public PurchaseItemDto toDto(PurchaseItem item) {
        if (item == null) {
            return null;
        }

        String productName = productFacade.getById(item.getProductId())
                .map(p -> p.name())
                .orElse("Producto Desconocido");

        return new PurchaseItemDto(
                item.getId(),
                item.getProductId(),
                productName,
                item.getQuantity(),
                item.getUnitCost(),
                item.getSubtotal()
        );
    }
}
