package com.sciencebot.pos.categories;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos completos de una categoría de productos")
public record CategoryDto(
    @Schema(description = "Identificador único de la categoría", example = "1")
    Long id,
    @Schema(description = "Nombre de la categoría", example = "Alimentos")
    String name,
    @Schema(description = "Descripción de la categoría", example = "Productos alimenticios y comestibles")
    String description,
    @Schema(description = "Número de productos asociados a esta categoría", example = "25")
    int productCount
) {}
