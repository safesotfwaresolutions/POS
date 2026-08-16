package com.sciencebot.pos.reports;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado de stock de un producto")
public record StockReportDto(
        @Schema(description = "ID del producto", example = "1")
        Long productId,
        @Schema(description = "Nombre del producto", example = "Arroz Diana 500g")
        String productName,
        @Schema(description = "Unidades disponibles en inventario", example = "15")
        int quantityAvailable,
        @Schema(description = "Stock mínimo configurado para alerta", example = "20")
        int minStock,
        @Schema(description = "Estado del stock: OK (sobre mínimo), LOW (bajo mínimo), OUT (sin stock)",
                example = "LOW", allowableValues = {"OK", "LOW", "OUT"})
        String status
) {}
