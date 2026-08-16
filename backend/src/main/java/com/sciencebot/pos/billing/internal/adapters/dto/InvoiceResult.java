package com.sciencebot.pos.billing.internal.adapters.dto;

import java.time.LocalDateTime;

public record InvoiceResult(
        boolean isSuccess,
        String legalInvoiceNumber,
        String cufe,
        String qrCode,
        String pdfUrl,
        String status, // VALIDATED, REJECTED, PENDING, ERROR
        String errorMessage,
        LocalDateTime validatedAt
) {
    public static InvoiceResult success(String legalInvoiceNumber, String cufe, String qrCode, String pdfUrl) {
        return new InvoiceResult(true, legalInvoiceNumber, cufe, qrCode, pdfUrl, "VALIDATED", null, LocalDateTime.now());
    }

    public static InvoiceResult pending(String errorMessage) {
        return new InvoiceResult(false, null, null, null, null, "PENDING", errorMessage, null);
    }

    public static InvoiceResult rejected(String errorMessage) {
        return new InvoiceResult(false, null, null, null, null, "REJECTED", errorMessage, null);
    }

    public static InvoiceResult error(String errorMessage) {
        return new InvoiceResult(false, null, null, null, null, "ERROR", errorMessage, null);
    }
}
