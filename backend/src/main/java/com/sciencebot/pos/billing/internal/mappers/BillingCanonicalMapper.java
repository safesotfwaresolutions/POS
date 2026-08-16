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
                "10", // 10 = Efectivo (Estándar DIAN)
                customerData,
                items,
                sale.totalAmount()
        );
    }

    private CustomerBillingData resolveCustomer(String customerIdentifier) {
        if (customerIdentifier != null && !customerIdentifier.isBlank() && !customerIdentifier.equalsIgnoreCase("Cliente General")) {
            Optional<CustomerDto> customerOpt = customerFacade.getByIdentification(customerIdentifier);
            if (customerOpt.isPresent()) {
                CustomerDto customer = customerOpt.get();
                return new CustomerBillingData(
                        customer.identification(),
                        "",
                        customer.fullName(),
                        (customer.email() != null && !customer.email().isBlank()) ? customer.email() : "cliente@tienda.com",
                        13, // 13 = Persona Natural (DIAN)
                        21, // 21 = Consumidor Final / No Responsable (DIAN)
                        null
                );
            }
        }

        // Consumidor Final genérico estándar DIAN
        return new CustomerBillingData(
                "222222222222",
                "",
                "Consumidor Final",
                "consumidorfinal@tienda.com",
                13,
                21,
                null
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
                        item.quantity(),
                        item.unitPrice(),
                        BigDecimal.ZERO,
                        item.subtotal()
                ));
            }
        }
        return list;
    }
}
