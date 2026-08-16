package com.sciencebot.pos.billing.internal.adapters.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceRequest(
        Long saleId,
        String invoiceNumber,
        String paymentMethodCode,
        CustomerBillingData customer,
        List<InvoiceItemData> items,
        BigDecimal totalAmount
) {}
