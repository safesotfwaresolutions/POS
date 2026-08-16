package com.sciencebot.pos.billing.internal.adapters.mock;

import com.sciencebot.pos.billing.internal.adapters.ElectronicInvoicingProvider;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador Mock para facturación electrónica simulada en memoria.
 * Permite ejecutar tests y trabajar en modo local/offline sin dependencias de red ni credenciales externas.
 */
@Component("mockBillingProvider")
public class MockBillingAdapter implements ElectronicInvoicingProvider {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public InvoiceResult emitInvoice(InvoiceRequest request) {
        if (request == null || request.saleId() == null) {
            return InvoiceResult.error("Solicitud de factura nula o sin ID de venta");
        }

        String legalNumber = "SETP-MOCK-" + String.format("%06d", request.saleId());
        String cufe = UUID.nameUUIDFromBytes(("MOCK-CUFE-" + request.saleId()).getBytes()).toString().replace("-", "") + "abcdef0123456789";
        String qrCode = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + cufe;
        String pdfUrl = "https://api-mock.local/v1/bills/" + legalNumber + "/pdf";

        return InvoiceResult.success(legalNumber, cufe, qrCode, pdfUrl);
    }

    @Override
    public InvoiceResult queryInvoice(String legalNumber) {
        String cufe = UUID.randomUUID().toString().replace("-", "");
        String qrCode = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + cufe;
        String pdfUrl = "https://api-mock.local/v1/bills/" + legalNumber + "/pdf";

        return InvoiceResult.success(legalNumber, cufe, qrCode, pdfUrl);
    }
}
