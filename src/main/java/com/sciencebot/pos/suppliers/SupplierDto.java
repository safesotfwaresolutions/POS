package com.sciencebot.pos.suppliers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos completos de un proveedor")
public record SupplierDto(
        @Schema(description = "Identificador único del proveedor", example = "1")
        Long id,
        @Schema(description = "Razón social de la empresa", example = "Distribuidora Central S.A.S.")
        String companyName,
        @Schema(description = "NIT o RUT de la empresa", example = "900123456-7")
        String taxId,
        @Schema(description = "Nombre del contacto principal", example = "María Rodríguez")
        String contactName,
        @Schema(description = "Correo electrónico de contacto", example = "contacto@distribcentral.com")
        String email,
        @Schema(description = "Teléfono de contacto", example = "+57 1 234 5678")
        String phone,
        @Schema(description = "Dirección de la empresa", example = "Zona Industrial # 15-30, Bogotá")
        String address,
        @Schema(description = "Si el proveedor está activo", example = "true")
        boolean active
) {}
