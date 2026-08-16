package com.sciencebot.pos.sales.internal.services;

import com.sciencebot.pos.sales.*;
import com.sciencebot.pos.sales.internal.entities.Sale;
import com.sciencebot.pos.sales.internal.entities.SaleItem;
import com.sciencebot.pos.sales.internal.repositories.SaleRepository;
import com.sciencebot.pos.sales.internal.mappers.SaleMapper;
import com.sciencebot.pos.customers.CustomerFacade;
import com.sciencebot.pos.customers.CustomerDto;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.products.ProductDto;
import com.sciencebot.pos.inventory.InventoryFacade;
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
public class SaleServiceImpl implements SaleFacade {

    private static final String GENERAL_CUSTOMER_IDENTIFICATION = "9999999999";

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;
    private final CustomerFacade customerFacade;
    private final ProductFacade productFacade;
    private final InventoryFacade inventoryFacade;
    private final UserFacade userFacade;
    private final com.sciencebot.pos.billing.BillingFacade billingFacade;

    public SaleServiceImpl(
            SaleRepository saleRepository,
            SaleMapper saleMapper,
            @Lazy CustomerFacade customerFacade,
            @Lazy ProductFacade productFacade,
            @Lazy InventoryFacade inventoryFacade,
            @Lazy UserFacade userFacade,
            @Lazy com.sciencebot.pos.billing.BillingFacade billingFacade
    ) {
        this.saleRepository = saleRepository;
        this.saleMapper = saleMapper;
        this.customerFacade = customerFacade;
        this.productFacade = productFacade;
        this.inventoryFacade = inventoryFacade;
        this.userFacade = userFacade;
        this.billingFacade = billingFacade;
    }


    @Override
    @Transactional
    public SaleDto registerSale(CreateSaleCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto");
        }
        if (command.cashReceived() == null || command.cashReceived().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El efectivo recibido no puede ser negativo");
        }

        // 1. Resolve customer
        final Long resolvedCustomerId;
        Long customerId = command.customerId();
        if (customerId == null) {
            CustomerDto generalCustomer = customerFacade.getByIdentification(GENERAL_CUSTOMER_IDENTIFICATION)
                    .orElseThrow(() -> new EntityNotFoundException("Cliente General no encontrado en el sistema"));
            resolvedCustomerId = generalCustomer.id();
        } else {
            final Long lookupId = customerId;
            CustomerDto customer = customerFacade.getById(lookupId)
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con ID: " + lookupId));
            if (!customer.active()) {
                throw new IllegalArgumentException("No se pueden realizar ventas a un cliente inactivo");
            }
            resolvedCustomerId = customer.id();
        }

        // Get current user
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "admin";
        Long userId = userFacade.findByUsername(username)
                .map(u -> u.id())
                .orElse(1L);

        Sale sale = new Sale();
        sale.setCustomerId(resolvedCustomerId);
        sale.setUserId(userId);
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setCashReceived(command.cashReceived());
        sale.setCashChange(BigDecimal.ZERO);

        // Generate invoice number from database sequence
        Long seq = saleRepository.getNextInvoiceSeq();
        String invoiceNumber = "FACT-" + String.format("%06d", seq);
        sale.setInvoiceNumber(invoiceNumber);

        BigDecimal total = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();

        for (CreateSaleItemCommand itemCommand : command.items()) {
            if (itemCommand.productId() == null) {
                throw new IllegalArgumentException("El ID del producto es obligatorio");
            }
            if (itemCommand.quantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            }

            // Verify product exists and is active
            ProductDto product = productFacade.getById(itemCommand.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + itemCommand.productId()));
            if (!product.active()) {
                throw new IllegalArgumentException("El producto " + product.name() + " está inactivo y no se puede vender");
            }

            // Verify stock (RN-INV-001)
            if (product.quantityAvailable() < itemCommand.quantity()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + product.name() + " (Disponible: " + product.quantityAvailable() + ")");
            }

            // Register movement in Inventory module (descuenta stock internamente)
            inventoryFacade.registerMovement(itemCommand.productId(), "VENTA", itemCommand.quantity(), "Venta registrada Factura: " + invoiceNumber);

            BigDecimal subtotal = product.salePrice().multiply(BigDecimal.valueOf(itemCommand.quantity()));
            total = total.add(subtotal);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProductId(itemCommand.productId());
            saleItem.setQuantity(itemCommand.quantity());
            saleItem.setUnitPrice(product.salePrice());
            saleItem.setSubtotal(subtotal);
            items.add(saleItem);
        }

        if (command.cashReceived().compareTo(total) < 0) {
            throw new IllegalArgumentException("El efectivo recibido (" + command.cashReceived() + ") es insuficiente para cubrir el total (" + total + ")");
        }

        sale.setTotalAmount(total);
        sale.setCashChange(command.cashReceived().subtract(total));
        sale.setItems(items);

        Sale saved = saleRepository.save(sale);
        SaleDto saleDto = saleMapper.toDto(saved);

        // Si sendToFactus es true, enviamos inmediatamente a Factus.
        // Si es false o no se envía, la venta queda guardada en POS para emisión manual posterior.
        if (Boolean.TRUE.equals(command.sendToFactus())) {
            try {
                billingFacade.processElectronicInvoice(saleDto);
            } catch (Exception e) {
                // Si la llamada remota a Factus falla, la venta local NO se revierte.
                // La factura electrónica queda registrada con estado PENDING para reintento/emisión posterior.
            }
        }

        return saleDto;
    }


    @Override
    public Optional<SaleDto> getById(Long id) {
        return saleRepository.findById(id).map(saleMapper::toDto);
    }

    @Override
    public Page<SaleDto> searchSales(
            Long customerId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        return saleRepository.searchSales(customerId, dateFrom, dateTo, pageable)
                .map(saleMapper::toDto);
    }
}
