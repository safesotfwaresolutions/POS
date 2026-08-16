package com.sciencebot.pos.inventory.internal.mappers;

import com.sciencebot.pos.inventory.InventoryMovementDto;
import com.sciencebot.pos.inventory.internal.entities.InventoryMovement;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.users.UserFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    private final ProductFacade productFacade;
    private final UserFacade userFacade;

    public InventoryMapper(@Lazy ProductFacade productFacade, @Lazy UserFacade userFacade) {
        this.productFacade = productFacade;
        this.userFacade = userFacade;
    }

    public InventoryMovementDto toDto(InventoryMovement movement) {
        if (movement == null) {
            return null;
        }

        String productName = productFacade.getById(movement.getProductId())
                .map(p -> p.name())
                .orElse("Producto Desconocido");

        String username = "Desconocido";
        try {
            username = userFacade.getById(movement.getUserId()).username();
        } catch (Exception e) {
            // Fallback
        }

        return new InventoryMovementDto(
                movement.getId(),
                movement.getProductId(),
                productName,
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPreviousStock(),
                movement.getNewStock(),
                movement.getReason(),
                username,
                movement.getCreatedAt()
        );
    }
}
