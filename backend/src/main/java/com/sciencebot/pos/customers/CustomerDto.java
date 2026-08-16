package com.sciencebot.pos.customers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos completos de un cliente")
public record CustomerDto(
        @Schema(description = "Identificador único del cliente", example = "1")
        Long id,
        @Schema(description = "Nombre completo del cliente", example = "Juan García López")
        String fullName,
        @Schema(description = "Número de identificación (cédula, NIT)", example = "1234567890")
        String identification,
        @Schema(description = "Correo electrónico", example = "juan.garcia@email.com")
        String email,
        @Schema(description = "Número de teléfono", example = "+57 310 123 4567")
        String phone,
        @Schema(description = "Dirección postal", example = "Calle 123 # 45-67, Bogotá")
        String address,
        @Schema(description = "Si el cliente está activo en el sistema", example = "true")
        boolean active
) {}
