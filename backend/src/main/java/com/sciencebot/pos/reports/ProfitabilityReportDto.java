package com.sciencebot.pos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Rentabilidad bruta de un producto en el período")
public record ProfitabilityReportDto(
        @Schema(description = "ID del producto", example = "1")
        Long productId,
        @Schema(description = "Nombre del producto", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Ganancia bruta total: (precio_venta - costo) × unidades_vendidas", example = "105000.00")
        BigDecimal profitAmount
) {}
