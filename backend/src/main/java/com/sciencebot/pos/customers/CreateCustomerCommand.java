package com.sciencebot.pos.customers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos requeridos para registrar un nuevo cliente")
public record CreateCustomerCommand(
        @Schema(description = "Nombre completo del cliente", example = "Juan García López", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,
        @Schema(description = "Número de identificación (cédula o NIT)", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
        String identification,
        @Schema(description = "Correo electrónico del cliente", example = "juan.garcia@email.com")
        String email,
        @Schema(description = "Número de teléfono", example = "+57 310 123 4567")
        String phone,
        @Schema(description = "Dirección del cliente", example = "Calle 123 # 45-67, Bogotá")
        String address
) {}
