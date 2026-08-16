package com.sciencebot.pos.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface InventoryFacade {
    InventoryMovementDto registerMovement(Long productId, String movementType, int quantity, String reason);
    Page<InventoryMovementDto> searchMovements(
            Long productId,
            String movementType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    );
}
