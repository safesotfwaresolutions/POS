package com.sciencebot.pos.purchases;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ítem de una orden de compra con totales")
public record PurchaseItemDto(
        @Schema(description = "Identificador del ítem", example = "1")
        Long id,
        @Schema(description = "ID del producto", example = "5")
        Long productId,
        @Schema(description = "Nombre del producto", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Cantidad comprada", example = "100")
        int quantity,
        @Schema(description = "Costo unitario en esta compra", example = "2500.00")
        BigDecimal unitCost,
        @Schema(description = "Subtotal (unitCost × quantity)", example = "250000.00")
        BigDecimal subtotal
) {}
