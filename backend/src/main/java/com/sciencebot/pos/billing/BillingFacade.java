package com.sciencebot.pos.billing;

import com.sciencebot.pos.sales.SaleDto;
import java.util.Optional;

public interface BillingFacade {
    ElectronicInvoiceDto processElectronicInvoice(SaleDto sale);
    Optional<ElectronicInvoiceDto> getBySaleId(Long saleId);
    ElectronicInvoiceDto retryInvoice(Long saleId);
}
