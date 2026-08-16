package com.sciencebot.pos.reports;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto en el ranking de más vendidos")
public record TopProductDto(
        @Schema(description = "ID del producto", example = "1")
        Long productId,
        @Schema(description = "Nombre del producto", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Total de unidades vendidas en el período", example = "350")
        long quantitySold
) {}
