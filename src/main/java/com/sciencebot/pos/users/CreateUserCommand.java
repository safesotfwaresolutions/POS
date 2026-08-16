package com.sciencebot.pos.users;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos requeridos para crear un nuevo usuario")
public record CreateUserCommand(
    @Schema(description = "Nombre completo del usuario", example = "Carlos Martínez", requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,
    @Schema(description = "Nombre de usuario único para login", example = "carlos.m", requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    @Schema(description = "Correo electrónico", example = "carlos@empresa.com")
    String email,
    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "MiClave123!", requiredMode = Schema.RequiredMode.REQUIRED)
    String password,
    @Schema(description = "Rol del usuario: ADMINISTRATOR, SUPERVISOR o SELLER",
            example = "SUPERVISOR", allowableValues = {"ADMINISTRATOR", "SUPERVISOR", "SELLER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    String role
) {}
