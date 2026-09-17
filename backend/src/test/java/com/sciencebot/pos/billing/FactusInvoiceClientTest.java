package com.sciencebot.pos.billing;

import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusClientHelper;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusInvoiceClient;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusSaleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactusInvoiceClientTest {

    @Mock
    private FactusClientHelper clientHelper;

    private FactusSaleMapper saleMapper;
    private FactusInvoiceClient invoiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        saleMapper = new FactusSaleMapper();
        invoiceClient = new FactusInvoiceClient(clientHelper, saleMapper, 8);
    }

    @Test
    void getProviderName_ReturnsFactus() {
        assertEquals("factus", invoiceClient.getProviderName());
    }

    @Test
    void emitInvoice_NullSaleId_ReturnsErrorResult() {
        InvoiceRequest request = new InvoiceRequest(null, "FACT-001", "10", null, List.of(), BigDecimal.TEN);
        InvoiceResult result = invoiceClient.emitInvoice(request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("ERROR", result.status());
        assertTrue(result.errorMessage().contains("ID de la venta es obligatorio"));
    }

    @Test
    void queryInvoice_ReturnsPending() {
        InvoiceResult result = invoiceClient.queryInvoice("FACT-001");
        assertNotNull(result);
        assertEquals("PENDING", result.status());
        assertTrue(result.errorMessage().contains("FACT-001"));
    }

    @Test
    void sendInvoiceEmail_NullArguments_ThrowsException() {
        assertThrows(NullPointerException.class, () -> invoiceClient.sendInvoiceEmail(null, "correo@tienda.com"));
        assertThrows(NullPointerException.class, () -> invoiceClient.sendInvoiceEmail("FACT-001", null));
    }
}
