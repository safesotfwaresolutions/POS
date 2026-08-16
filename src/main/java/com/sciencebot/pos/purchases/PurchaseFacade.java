package com.sciencebot.pos.purchases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PurchaseFacade {
    PurchaseDto registerPurchase(CreatePurchaseCommand command);
    Optional<PurchaseDto> getById(Long id);
    Page<PurchaseDto> searchPurchases(
            Long supplierId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );
}
