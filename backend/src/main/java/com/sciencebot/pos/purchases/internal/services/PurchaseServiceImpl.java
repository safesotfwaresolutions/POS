package com.sciencebot.pos.purchases.internal.services;

import com.sciencebot.pos.purchases.*;
import com.sciencebot.pos.purchases.internal.entities.Purchase;
import com.sciencebot.pos.purchases.internal.entities.PurchaseItem;
import com.sciencebot.pos.purchases.internal.repositories.PurchaseRepository;
import com.sciencebot.pos.purchases.internal.mappers.PurchaseMapper;
import com.sciencebot.pos.inventory.InventoryFacade;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.suppliers.SupplierFacade;
import com.sciencebot.pos.suppliers.SupplierDto;
import com.sciencebot.pos.users.UserFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PurchaseServiceImpl implements PurchaseFacade {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final SupplierFacade supplierFacade;
    private final ProductFacade productFacade;
    private final InventoryFacade inventoryFacade;
    private final UserFacade userFacade;

    public PurchaseServiceImpl(
            PurchaseRepository purchaseRepository,
            PurchaseMapper purchaseMapper,
            @Lazy SupplierFacade supplierFacade,
            @Lazy ProductFacade productFacade,
            @Lazy InventoryFacade inventoryFacade,
            @Lazy UserFacade userFacade
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseMapper = purchaseMapper;
        this.supplierFacade = supplierFacade;
        this.productFacade = productFacade;
        this.inventoryFacade = inventoryFacade;
        this.userFacade = userFacade;
    }

    @Override
    @Transactional
    public PurchaseDto registerPurchase(CreatePurchaseCommand command) {
        if (command.supplierId() == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un ítem");
        }

        // 1. Validate active supplier (RN-SUPP-001)
        SupplierDto supplier = supplierFacade.getById(command.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con ID: " + command.supplierId()));
        if (!supplier.active()) {
            throw new IllegalArgumentException("No se pueden asociar compras a proveedores inactivos (RN-SUPP-001)");
        }

        // Get current user
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "admin";
        Long userId = userFacade.findByUsername(username)
                .map(u -> u.id())
                .orElse(1L);

        Purchase purchase = new Purchase();
        purchase.setSupplierId(command.supplierId());
        purchase.setInvoiceNumber(command.invoiceNumber() != null ? command.invoiceNumber().trim() : null);
        purchase.setUserId(userId);
        purchase.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseItem> items = new ArrayList<>();

        for (CreatePurchaseItemCommand itemCommand : command.items()) {
            if (itemCommand.productId() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio en los ítems");
            }
            if (itemCommand.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad en cada ítem debe ser mayor a cero");
            }
            if (itemCommand.unitCost() == null || itemCommand.unitCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El costo unitario de los productos no puede ser negativo");
            }

            // Verify product exists
            productFacade.getById(itemCommand.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + itemCommand.productId()));

            // Update product purchase price (RN-PUR-002)
            productFacade.updatePurchasePrice(itemCommand.productId(), itemCommand.unitCost());

            // Register movement in Inventory module (incrementa stock internamente)
            String movementReason = "Compra registrada " + (command.invoiceNumber() != null ? "Factura: " + command.invoiceNumber() : "");
            inventoryFacade.registerMovement(itemCommand.productId(), "COMPRA", itemCommand.quantity(), movementReason);

            BigDecimal subtotal = itemCommand.unitCost().multiply(BigDecimal.valueOf(itemCommand.quantity()));
            total = total.add(subtotal);

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setPurchase(purchase);
            purchaseItem.setProductId(itemCommand.productId());
            purchaseItem.setQuantity(itemCommand.quantity());
            purchaseItem.setUnitCost(itemCommand.unitCost());
            purchaseItem.setSubtotal(subtotal);
            items.add(purchaseItem);
        }

        purchase.setTotalAmount(total);
        purchase.setItems(items);

        Purchase saved = purchaseRepository.save(purchase);
        return purchaseMapper.toDto(saved);
    }

    @Override
    public Optional<PurchaseDto> getById(Long id) {
        return purchaseRepository.findById(id).map(purchaseMapper::toDto);
    }

    @Override
    public Page<PurchaseDto> searchPurchases(
            Long supplierId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        return purchaseRepository.searchPurchases(supplierId, dateFrom, dateTo, pageable)
                .map(purchaseMapper::toDto);
    }
}
