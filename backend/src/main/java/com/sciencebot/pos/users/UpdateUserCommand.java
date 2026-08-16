package com.sciencebot.pos.users;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos actualizables de un usuario existente")
public record UpdateUserCommand(
    @Schema(description = "Nuevo nombre completo", example = "Carlos Martínez Pérez")
    String fullName,
    @Schema(description = "Nuevo correo electrónico", example = "carlos.nuevo@empresa.com")
    String email,
    @Schema(description = "Nuevo rol: ADMINISTRATOR, SUPERVISOR o SELLER",
            example = "SUPERVISOR", allowableValues = {"ADMINISTRATOR", "SUPERVISOR", "SELLER"})
    String role
) {}
