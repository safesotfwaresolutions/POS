package com.sciencebot.pos.billing;

import com.sciencebot.pos.sales.SaleDto;
import java.util.List;
import java.util.Optional;

public interface BillingFacade {
    ElectronicInvoiceDto processElectronicInvoice(SaleDto sale);
    Optional<ElectronicInvoiceDto> getBySaleId(Long saleId);
    ElectronicInvoiceDto retryInvoice(Long saleId);

    void sendInvoiceEmail(Long saleId, String email);

    List<NumberingRangeDto> queryDianNumberingRanges();
    List<NumberingRangeDto> listNumberingRanges();
    NumberingRangeDto getNumberingRange(Long numberingRangeId);
    NumberingRangeDto createNumberingRange(CreateNumberingRangeRequest request);
    boolean deleteNumberingRange(Long numberingRangeId);
    boolean toggleNumberingRangeStatus(Long numberingRangeId);
}

