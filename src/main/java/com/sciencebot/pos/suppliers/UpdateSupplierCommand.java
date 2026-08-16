package com.sciencebot.pos.suppliers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos actualizables de un proveedor existente")
public record UpdateSupplierCommand(
        @Schema(description = "Razón social de la empresa", example = "Distribuidora Central S.A.S.")
        String companyName,
        @Schema(description = "Nombre del contacto principal", example = "María Rodríguez")
        String contactName,
        @Schema(description = "Correo electrónico de contacto", example = "contacto@distribcentral.com")
        String email,
        @Schema(description = "Teléfono de contacto", example = "+57 1 234 5678")
        String phone,
        @Schema(description = "Dirección de la empresa", example = "Zona Industrial # 15-30, Bogotá")
        String address
) {}
