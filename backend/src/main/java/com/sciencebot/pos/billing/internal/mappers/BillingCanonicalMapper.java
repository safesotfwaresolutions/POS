package com.sciencebot.pos.billing.internal.mappers;

import com.sciencebot.pos.billing.internal.adapters.dto.CustomerBillingData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceItemData;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.customers.CustomerDto;
import com.sciencebot.pos.customers.CustomerFacade;
import com.sciencebot.pos.sales.SaleDto;
import com.sciencebot.pos.sales.SaleItemDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BillingCanonicalMapper {

    private final CustomerFacade customerFacade;

    public BillingCanonicalMapper(@Lazy CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    public InvoiceRequest toInvoiceRequest(SaleDto sale) {
        if (sale == null) {
            return null;
        }

        CustomerBillingData customerData = resolveCustomer(sale.customerName());
        List<InvoiceItemData> items = mapItems(sale.items());

        return new InvoiceRequest(
                sale.id(),
                sale.invoiceNumber(),
                "1",  // 1 = Contado (Estándar DIAN)
                "10", // 10 = Efectivo (Estándar DIAN)
                customerData,
                items,
                sale.totalAmount(),
                "Factura emitida desde Sistema POS"
        );
    }

    private CustomerBillingData resolveCustomer(String customerIdentifier) {
        if (customerIdentifier != null && !customerIdentifier.isBlank() && !customerIdentifier.equalsIgnoreCase("Cliente General")) {
            Optional<CustomerDto> customerOpt = customerFacade.getByIdentification(customerIdentifier);
            if (customerOpt.isPresent()) {
                CustomerDto customer = customerOpt.get();
                String rawId = customer.identification() != null ? customer.identification().trim() : "";
                
                // Limpieza de identificación (sin guión ni DV si vinieran incluidos)
                String idClean = rawId.replaceAll("[^0-9a-zA-Z]", "");
                String dv = "";
                if (rawId.contains("-")) {
                    String[] parts = rawId.split("-");
                    if (parts.length > 1) {
                        idClean = parts[0].trim();
                        dv = parts[1].trim();
                    }
                }

                // Determinación de persona jurídica vs natural
                boolean isNit = idClean.length() == 9 || (rawId.contains("-") && !dv.isBlank());
                String docCode = isNit ? "31" : "13"; // 31 = NIT, 13 = Cédula de ciudadanía
                String orgCode = isNit ? "1" : "2";   // 1 = Jurídica, 2 = Natural
                String company = isNit ? customer.fullName() : null;
                String names = !isNit ? customer.fullName() : null;

                return new CustomerBillingData(
                        docCode,
                        idClean,
                        dv,
                        orgCode,
                        "ZZ",
                        List.of("R-99-PN"),
                        names,
                        company,
                        null,
                        (customer.email() != null && !customer.email().isBlank()) ? customer.email() : "cliente@tienda.com",
                        customer.phone(),
                        customer.address(),
                        "CO",
                        "11001" // Código estándar Bogotá / Municipio
                );
            }
        }

        // Consumidor Final genérico estándar DIAN
        return new CustomerBillingData(
                "13",
                "222222222222",
                "",
                "2",
                "ZZ",
                List.of("R-99-PN"),
                "Consumidor Final",
                null,
                null,
                "consumidorfinal@tienda.com",
                null,
                null,
                "CO",
                "11001"
        );
    }

    private List<InvoiceItemData> mapItems(List<SaleItemDto> items) {
        List<InvoiceItemData> list = new ArrayList<>();
        if (items != null) {
            for (SaleItemDto item : items) {
                String cleanName = item.productName() != null ? item.productName().replaceAll("[^a-zA-Z0-9]", "") : "PROD";
                String productCode = "REF-" + cleanName;
                list.add(new InvoiceItemData(
                        productCode,
                        item.productName(),
                        BigDecimal.valueOf(item.quantity()),
                        item.unitPrice(),
                        BigDecimal.ZERO,
                        item.subtotal(),
                        "94",  // 94 = Unidad estándar DIAN
                        "999", // 999 = Estándar del contribuyente
                        "01",  // 01 = IVA estándar DIAN
                        BigDecimal.ZERO,
                        false
                ));
            }
        }
        return list;
    }
}
