package com.sciencebot.pos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Reporte de ventas para un período determinado")
public record SalesReportDto(
        @Schema(description = "Inicio del período analizado", example = "2025-01-01T00:00:00")
        LocalDateTime dateFrom,
        @Schema(description = "Fin del período analizado", example = "2025-12-31T23:59:59")
        LocalDateTime dateTo,
        @Schema(description = "Monto total vendido en el período", example = "85000000.00")
        BigDecimal totalSold,
        @Schema(description = "Número total de transacciones de venta", example = "1250")
        long transactionCount,
        @Schema(description = "Desglose de ventas por vendedor (username → monto total)")
        Map<String, BigDecimal> salesBySeller
) {}
