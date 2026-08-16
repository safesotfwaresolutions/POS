package com.sciencebot.pos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Reporte de compras para un período determinado")
public record PurchasesReportDto(
        @Schema(description = "Inicio del período analizado", example = "2025-01-01T00:00:00")
        LocalDateTime dateFrom,
        @Schema(description = "Fin del período analizado", example = "2025-12-31T23:59:59")
        LocalDateTime dateTo,
        @Schema(description = "Monto total invertido en compras", example = "15000000.00")
        BigDecimal totalInverted,
        @Schema(description = "Desglose del monto de compras por proveedor (nombre proveedor → monto)")
        Map<String, BigDecimal> purchasesBySupplier
) {}
