package com.sciencebot.pos.purchases;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Datos completos de una orden de compra")
public record PurchaseDto(
        @Schema(description = "Identificador único de la compra", example = "1")
        Long id,
        @Schema(description = "ID del proveedor", example = "2")
        Long supplierId,
        @Schema(description = "Nombre del proveedor", example = "Distribuidora Central S.A.S.")
        String supplierName,
        @Schema(description = "Número de factura del proveedor", example = "FV-2025-001234")
        String invoiceNumber,
        @Schema(description = "Monto total de la compra", example = "1250000.00")
        BigDecimal totalAmount,
        @Schema(description = "Username del usuario que registró la compra", example = "supervisor01")
        String user,
        @Schema(description = "Fecha y hora de registro de la compra", example = "2025-03-15T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Lista de productos comprados")
        List<PurchaseItemDto> items
) {}
