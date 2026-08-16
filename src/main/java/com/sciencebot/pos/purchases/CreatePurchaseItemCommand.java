package com.sciencebot.pos.purchases;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ítem de una compra")
public record CreatePurchaseItemCommand(
        @Schema(description = "ID del producto comprado", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,
        @Schema(description = "Cantidad de unidades compradas", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        int quantity,
        @Schema(description = "Costo unitario del producto en esta compra", example = "2500.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal unitCost
) {}
