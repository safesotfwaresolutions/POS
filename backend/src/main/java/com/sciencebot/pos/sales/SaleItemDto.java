package com.sciencebot.pos.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ítem de una venta con totales calculados")
public record SaleItemDto(
        @Schema(description = "Identificador del ítem", example = "1")
        Long id,
        @Schema(description = "ID del producto vendido", example = "1")
        Long productId,
        @Schema(description = "Nombre del producto", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Cantidad vendida", example = "3")
        int quantity,
        @Schema(description = "Precio unitario al momento de la venta", example = "3200.00")
        BigDecimal unitPrice,
        @Schema(description = "Subtotal del ítem (unitPrice × quantity)", example = "9600.00")
        BigDecimal subtotal
) {}
