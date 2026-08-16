package com.sciencebot.pos.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Registro de un movimiento de inventario")
public record InventoryMovementDto(
        @Schema(description = "Identificador del movimiento", example = "1")
        Long id,
        @Schema(description = "ID del producto afectado", example = "5")
        Long productId,
        @Schema(description = "Nombre del producto afectado", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Tipo de movimiento: ENTRY, EXIT, ADJUSTMENT, RETURN", example = "ENTRY")
        String movementType,
        @Schema(description = "Cantidad de unidades movidas", example = "100")
        int quantity,
        @Schema(description = "Stock antes del movimiento", example = "50")
        int previousStock,
        @Schema(description = "Stock después del movimiento", example = "150")
        int newStock,
        @Schema(description = "Motivo del movimiento", example = "Recepción de mercancía proveedor")
        String reason,
        @Schema(description = "Username del usuario que registró el movimiento", example = "supervisor01")
        String user,
        @Schema(description = "Fecha y hora del movimiento", example = "2025-03-15T09:00:00")
        LocalDateTime createdAt
) {}
