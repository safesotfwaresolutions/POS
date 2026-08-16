package com.sciencebot.pos.categories;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar una categoría existente")
public record UpdateCategoryCommand(
    @Schema(description = "Nuevo nombre de la categoría", example = "Alimentos y Bebidas")
    String name,
    @Schema(description = "Nueva descripción de la categoría", example = "Productos alimenticios, bebidas y comestibles")
    String description
) {}
