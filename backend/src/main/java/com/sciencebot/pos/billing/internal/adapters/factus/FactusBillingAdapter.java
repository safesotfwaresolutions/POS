package com.sciencebot.pos.billing.internal.adapters.factus;

import com.sciencebot.pos.billing.CreateNumberingRangeRequest;
import com.sciencebot.pos.billing.NumberingRangeDto;
import com.sciencebot.pos.billing.internal.adapters.ElectronicInvoicingProvider;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Adaptador de Facturación Electrónica para Factus (API REST DIAN v2).
 * Compone y delega a {@link FactusInvoiceClient} y {@link FactusNumberingRangeClient}.
 */
@Component("factusBillingProvider")
public class FactusBillingAdapter implements ElectronicInvoicingProvider {

    private final FactusInvoiceClient invoiceClient;
    private final FactusNumberingRangeClient rangeClient;

    @Autowired
    public FactusBillingAdapter(FactusInvoiceClient invoiceClient, FactusNumberingRangeClient rangeClient) {
        this.invoiceClient = invoiceClient;
        this.rangeClient = rangeClient;
    }

    // Constructores de compatibilidad y testing
    public FactusBillingAdapter(
            FactusTokenManager tokenManager,
            FactusSaleMapper saleMapper,
            FactusNumberingRangeMapper rangeMapper
    ) {
        FactusClientHelper helper = new FactusClientHelper(tokenManager);
        this.invoiceClient = new FactusInvoiceClient(helper, saleMapper);
        this.rangeClient = new FactusNumberingRangeClient(helper, rangeMapper);
    }

    public FactusBillingAdapter(
            FactusTokenManager tokenManager,
            FactusSaleMapper saleMapper
    ) {
        this(tokenManager, saleMapper, new FactusNumberingRangeMapper());
    }

    public FactusBillingAdapter(
            FactusTokenManager tokenManager,
            FactusSaleMapper saleMapper,
            FactusNumberingRangeMapper rangeMapper,
            RestClient restClient,
            String factusUrl,
            int numberingRangeId
    ) {
        FactusClientHelper helper = new FactusClientHelper(tokenManager, restClient, factusUrl);
        this.invoiceClient = new FactusInvoiceClient(helper, saleMapper, numberingRangeId);
        this.rangeClient = new FactusNumberingRangeClient(helper, rangeMapper);
    }

    @Override
    public String getProviderName() {
        return "factus";
    }

    // --- Facturas (delegado a FactusInvoiceClient) ---

    @Override
    public InvoiceResult emitInvoice(InvoiceRequest request) {
        return invoiceClient.emitInvoice(request);
    }

    @Override
    public InvoiceResult queryInvoice(String legalNumber) {
        return invoiceClient.queryInvoice(legalNumber);
    }

    @Override
    public byte[] downloadInvoicePDF(String legalNumber) {
        return invoiceClient.downloadInvoicePDF(legalNumber);
    }

    @Override
    public byte[] downloadInvoiceXML(String legalNumber) {
        return invoiceClient.downloadInvoiceXML(legalNumber);
    }

    @Override
    public boolean deleteUnvalidatedInvoice(String legalNumber) {
        return invoiceClient.deleteUnvalidatedInvoice(legalNumber);
    }

    @Override
    public boolean sendInvoiceEmail(String legalNumber, String email) {
        return invoiceClient.sendInvoiceEmail(legalNumber, email);
    }

    // --- Rangos de Numeración (delegado a FactusNumberingRangeClient) ---

    @Override
    public List<NumberingRangeDto> queryDianNumberingRanges() {
        return rangeClient.queryDianNumberingRanges();
    }

    @Override
    public List<NumberingRangeDto> listNumberingRanges() {
        return rangeClient.listNumberingRanges();
    }

    @Override
    public NumberingRangeDto getNumberingRange(Long numberingRangeId) {
        return rangeClient.getNumberingRange(numberingRangeId);
    }

    @Override
    public NumberingRangeDto createNumberingRange(CreateNumberingRangeRequest request) {
        return rangeClient.createNumberingRange(request);
    }

    @Override
    public boolean deleteNumberingRange(Long numberingRangeId) {
        return rangeClient.deleteNumberingRange(numberingRangeId);
    }

    @Override
    public boolean toggleNumberingRangeStatus(Long numberingRangeId) {
        return rangeClient.toggleNumberingRangeStatus(numberingRangeId);
    }
}
