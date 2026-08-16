package com.sciencebot.pos.products;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Datos requeridos para crear un nuevo producto")
public record CreateProductCommand(
    @Schema(description = "Código interno de referencia único", example = "PROD-001", requiredMode = Schema.RequiredMode.REQUIRED)
    String internalCode,
    @Schema(description = "Código de barras EAN/UPC (opcional)", example = "7702011477014")
    String barcode,
    @Schema(description = "Nombre del producto", example = "Arroz Diana 500g", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    @Schema(description = "ID de la categoría a la que pertenece", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Long categoryId,
    @Schema(description = "Precio de compra (costo unitario)", example = "2500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal purchasePrice,
    @Schema(description = "Precio de venta al público", example = "3200.00", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal salePrice,
    @Schema(description = "Stock mínimo para alerta de reabastecimiento", example = "20")
    int minStock,
    @Schema(description = "Descripción detallada del producto", example = "Arroz blanco de primera calidad")
    String description,
    @Schema(description = "URL de imagen del producto", example = "https://storage.example.com/products/arroz.jpg")
    String imageUrl
) {}
