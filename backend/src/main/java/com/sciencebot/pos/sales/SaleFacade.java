package com.sciencebot.pos.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SaleFacade {
    SaleDto registerSale(CreateSaleCommand command);
    Optional<SaleDto> getById(Long id);
    Page<SaleDto> searchSales(
            Long customerId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );
}
