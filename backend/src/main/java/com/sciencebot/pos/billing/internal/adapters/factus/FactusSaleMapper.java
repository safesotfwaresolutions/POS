package com.sciencebot.pos.billing.internal.adapters.factus;

import com.sciencebot.pos.billing.internal.adapters.dto.CustomerBillingData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceItemData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class FactusSaleMapper {

    /**
     * Transforma una solicitud canónica de factura en el payload JSON esperado por Factus API.
     */
    public Map<String, Object> toFactusRequestPayload(InvoiceRequest request, int numberingRangeId) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("numbering_range_id", numberingRangeId);
        String referenceCode = (request.invoiceNumber() != null ? request.invoiceNumber() : "FACT") + "-" + System.currentTimeMillis();
        payload.put("reference_code", referenceCode);
        payload.put("payment_method_code", request.paymentMethodCode() != null ? request.paymentMethodCode() : "10");
        payload.put("customer", mapCustomer(request.customer()));
        payload.put("items", mapItems(request.items()));

        return payload;
    }

    private Map<String, Object> mapCustomer(CustomerBillingData customer) {
        Map<String, Object> customerMap = new LinkedHashMap<>();
        if (customer != null) {
            customerMap.put("identification", customer.identification() != null ? customer.identification() : "222222222222");
            customerMap.put("dv", customer.dv() != null ? customer.dv() : "");
            customerMap.put("company", customer.legalName() != null ? customer.legalName() : "Consumidor Final");
            customerMap.put("email", customer.email() != null ? customer.email() : "cliente@tienda.com");
            customerMap.put("legal_organization_id", customer.legalOrganizationId() != null ? customer.legalOrganizationId() : 13);
            customerMap.put("tribute_id", customer.tributeId() != null ? customer.tributeId() : 21);
        } else {
            customerMap.put("identification", "222222222222");
            customerMap.put("dv", "");
            customerMap.put("company", "Consumidor Final");
            customerMap.put("email", "consumidorfinal@tienda.com");
            customerMap.put("legal_organization_id", 13);
            customerMap.put("tribute_id", 21);
        }
        return customerMap;
    }

    private List<Map<String, Object>> mapItems(List<InvoiceItemData> items) {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (items != null) {
            for (InvoiceItemData item : items) {
                itemsList.add(mapSingleItem(item));
            }
        }
        return itemsList;
    }

    private Map<String, Object> mapSingleItem(InvoiceItemData item) {
        Map<String, Object> itemMap = new LinkedHashMap<>();

        String cleanName = item.productName() != null ? item.productName().replaceAll("[^a-zA-Z0-9]", "") : "PROD";
        String codeRef = "REF-" + cleanName + "-" + System.currentTimeMillis();

        itemMap.put("code_reference", codeRef);
        itemMap.put("name", item.productName());
        itemMap.put("quantity", item.quantity());
        itemMap.put("discount_rate", 0);
        itemMap.put("price", item.unitPrice());
        itemMap.put("tax_rate", item.taxRate() != null ? item.taxRate().toPlainString() : "0.00");
        itemMap.put("unit_measure_id", 70); // 70 = Unidad estándar DIAN
        itemMap.put("standard_code_id", 1);
        itemMap.put("is_excluded", 0);        // 0 = No excluido
        itemMap.put("tribute_id", 1);         // 1 = IVA General estándar DIAN
        return itemMap;
    }
}
