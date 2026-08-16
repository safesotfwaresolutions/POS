package com.sciencebot.pos.categories;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos requeridos para crear una nueva categoría")
public record CreateCategoryCommand(
    @Schema(description = "Nombre de la categoría", example = "Alimentos", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,
    @Schema(description = "Descripción opcional de la categoría", example = "Productos alimenticios y comestibles")
    String description
) {}
