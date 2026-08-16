package com.sciencebot.pos.billing;

import com.sciencebot.pos.billing.internal.adapters.dto.CustomerBillingData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceItemData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusBillingAdapter;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusSaleMapper;
import com.sciencebot.pos.billing.internal.adapters.factus.FactusTokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactusBillingAdapterTest {

    @Mock
    private FactusTokenManager tokenManager;

    private FactusSaleMapper saleMapper;
    private FactusBillingAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        saleMapper = new FactusSaleMapper();
        adapter = new FactusBillingAdapter(tokenManager, saleMapper);
    }

    @Test
    void getProviderName_ReturnsFactus() {
        assertEquals("factus", adapter.getProviderName());
    }

    @Test
    void emitInvoice_NullSaleId_ReturnsErrorResult() {
        InvoiceRequest request = new InvoiceRequest(null, "FACT-001", "10", null, List.of(), BigDecimal.TEN);
        InvoiceResult result = adapter.emitInvoice(request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("ERROR", result.status());
        assertTrue(result.errorMessage().contains("ID de la venta es obligatorio"));
    }

    @Test
    void saleMapper_TransformsInvoiceRequest_Correctly() {
        CustomerBillingData customer = new CustomerBillingData("123456789", "1", "Empresa XYZ", "info@xyz.com", 1, 1, 11001);
        InvoiceItemData item = new InvoiceItemData("REF-01", "Laptop", 2, BigDecimal.valueOf(1500.0), BigDecimal.ZERO, BigDecimal.valueOf(3000.0));
        InvoiceRequest request = new InvoiceRequest(10L, "FACT-000010", "10", customer, List.of(item), BigDecimal.valueOf(3000.0));

        var payload = saleMapper.toFactusRequestPayload(request, 8);

        assertNotNull(payload);
        assertEquals(8, payload.get("numbering_range_id"));
        assertEquals("10", payload.get("payment_method_code"));
        assertTrue(payload.containsKey("customer"));
        assertTrue(payload.containsKey("items"));
    }
}
