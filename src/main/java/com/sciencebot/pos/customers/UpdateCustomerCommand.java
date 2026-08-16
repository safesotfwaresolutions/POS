package com.sciencebot.pos.customers;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos actualizables de un cliente existente")
public record UpdateCustomerCommand(
        @Schema(description = "Nombre completo del cliente", example = "Juan García López")
        String fullName,
        @Schema(description = "Correo electrónico", example = "juan.garcia@email.com")
        String email,
        @Schema(description = "Número de teléfono", example = "+57 310 123 4567")
        String phone,
        @Schema(description = "Dirección postal", example = "Calle 123 # 45-67, Bogotá")
        String address
) {}
