package com.sciencebot.pos.billing.internal.adapters.dto;

import java.math.BigDecimal;

public record InvoiceItemData(
        String productCode,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal subtotal
) {}
