package com.sciencebot.pos.billing.internal.adapters.factus;

import com.sciencebot.pos.billing.internal.adapters.ElectronicInvoicingProvider;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

/**
 * Adaptador de Facturación Electrónica para el proveedor Factus (API REST DIAN).
 * Encapsula la comunicación HTTP, autenticación OAuth2 y normalización de errores de Factus.
 */
@Component("factusBillingProvider")
public class FactusBillingAdapter implements ElectronicInvoicingProvider {

    private static final Logger log = LoggerFactory.getLogger(FactusBillingAdapter.class);

    @Value("${factus.url:https://api-sandbox.factus.com.co}")
    private String factusUrl;

    @Value("${factus.numbering-range-id:1}")
    private int numberingRangeId;

    private final FactusTokenManager tokenManager;
    private final FactusSaleMapper saleMapper;
    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Autowired
    public FactusBillingAdapter(
            FactusTokenManager tokenManager,
            FactusSaleMapper saleMapper
    ) {
        this.tokenManager = tokenManager;
        this.saleMapper = saleMapper;
        this.restClient = RestClient.create();
    }

    // Constructor para inyección en tests
    public FactusBillingAdapter(
            FactusTokenManager tokenManager,
            FactusSaleMapper saleMapper,
            RestClient restClient,
            String factusUrl,
            int numberingRangeId
    ) {
        this.tokenManager = tokenManager;
        this.saleMapper = saleMapper;
        this.restClient = restClient;
        this.factusUrl = factusUrl;
        this.numberingRangeId = numberingRangeId;
    }

    @Override
    public String getProviderName() {
        return "factus";
    }

    @Override
    public InvoiceResult emitInvoice(InvoiceRequest request) {
        Objects.requireNonNull(request, "La solicitud de factura no puede ser nula");
        if (request.saleId() == null) {
            return InvoiceResult.error("El ID de la venta es obligatorio para emitir la factura electrónica");
        }

        try {
            String token = tokenManager.getAccessToken();
            Map<String, Object> payload = saleMapper.toFactusRequestPayload(request, numberingRangeId);

            String targetUrl = (factusUrl != null && !factusUrl.isBlank())
                    ? factusUrl
                    : "https://api-sandbox.factus.com.co";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri(targetUrl + "/v1/bills/validate")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (isBillResponseValid(response)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                @SuppressWarnings("unchecked")
                Map<String, Object> bill = (Map<String, Object>) data.get("bill");

                String number = (String) bill.get("number");
                String cufe = (String) bill.get("cufe");
                String qr = (String) bill.get("qr");
                String publicUrl = (String) bill.get("public_url");

                return InvoiceResult.success(number, cufe, qr, publicUrl);
            } else {
                return InvoiceResult.error("Respuesta de Factus incompleta o sin nodo 'bill'");
            }

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                return InvoiceResult.pending("Fallo de autenticación con Factus API (HTTP " + ex.getStatusCode().value() + ")");
            } else {
                return InvoiceResult.rejected(truncateError("Error Validación DIAN (HTTP " + ex.getStatusCode().value() + "): " + ex.getResponseBodyAsString()));
            }
        } catch (HttpServerErrorException | ResourceAccessException ex) {
            return InvoiceResult.pending(truncateError("Factus no disponible / Timeout: " + ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error imprevisto al emitir factura electrónica en Factus para venta ID: {}", request.saleId(), ex);
            return InvoiceResult.error(truncateError("Error interno: " + ex.getMessage()));
        }
    }

    @Override
    public InvoiceResult queryInvoice(String legalNumber) {
        // Consulta simplificada para el adaptador de Factus
        return InvoiceResult.pending("Consulta de estado directo no implementada para número: " + legalNumber);
    }

    private boolean isBillResponseValid(Map<String, Object> response) {
        if (response == null || !response.containsKey("data") || response.get("data") == null) {
            return false;
        }
        if (!(response.get("data") instanceof Map<?, ?> data)) {
            return false;
        }
        return data.containsKey("bill") && data.get("bill") instanceof Map<?, ?>;
    }

    private String truncateError(String message) {
        if (message == null) return null;
        return message.length() > 1000 ? message.substring(0, 997) + "..." : message;
    }
}
