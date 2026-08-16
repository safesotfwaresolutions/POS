package com.sciencebot.pos.users;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos completos de un usuario del sistema")
public record UserDto(
    @Schema(description = "Identificador único del usuario", example = "1")
    Long id,
    @Schema(description = "Nombre completo del usuario", example = "Carlos Martínez")
    String fullName,
    @Schema(description = "Nombre de usuario para login", example = "carlos.m")
    String username,
    @Schema(description = "Correo electrónico", example = "carlos@empresa.com")
    String email,
    @Schema(description = "Rol del usuario en el sistema: ADMINISTRATOR, SUPERVISOR, SELLER",
            example = "SUPERVISOR", allowableValues = {"ADMINISTRATOR", "SUPERVISOR", "SELLER"})
    String role,
    @Schema(description = "Si el usuario puede acceder al sistema", example = "true")
    boolean active
) {}
