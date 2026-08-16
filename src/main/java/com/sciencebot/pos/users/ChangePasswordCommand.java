package com.sciencebot.pos.users;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para cambiar la contraseña de un usuario")
public record ChangePasswordCommand(
    @Schema(description = "Contraseña actual (opcional si es ADMINISTRATOR cambiando la de otro usuario)", example = "ClaveActual123!")
    String currentPassword,
    @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "NuevaClave456!", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword
) {}
