package com.sciencebot.pos.purchases;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Datos requeridos para registrar una nueva compra")
public record CreatePurchaseCommand(
        @Schema(description = "ID del proveedor al que se realiza la compra", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long supplierId,
        @Schema(description = "Número de factura del proveedor", example = "FV-2025-001234", requiredMode = Schema.RequiredMode.REQUIRED)
        String invoiceNumber,
        @Schema(description = "Lista de productos comprados con cantidades y costos", requiredMode = Schema.RequiredMode.REQUIRED)
        List<CreatePurchaseItemCommand> items
) {}
