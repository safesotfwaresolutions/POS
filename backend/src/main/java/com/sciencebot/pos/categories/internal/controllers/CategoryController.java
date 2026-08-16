package com.sciencebot.pos.categories.internal.controllers;

import com.sciencebot.pos.categories.CategoryDto;
import com.sciencebot.pos.categories.CategoryFacade;
import com.sciencebot.pos.categories.CreateCategoryCommand;
import com.sciencebot.pos.categories.UpdateCategoryCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "🏷️ Categorías", description = "Gestión de categorías de productos")
public class CategoryController {

    private final CategoryFacade categoryFacade;

    public CategoryController(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Listar todas las categorías",
            description = "Retorna la lista completa de categorías con su conteo de productos asociados. Accesible por todos los roles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de categorías",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<List<CategoryDto>> listAll() {
        return ResponseEntity.ok(categoryFacade.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Crear categoría",
            description = "Crea una nueva categoría de productos. Requiere rol **ADMINISTRATOR** o **SUPERVISOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CreateCategoryCommand command) {
        CategoryDto created = categoryFacade.createCategory(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza el nombre y descripción de una categoría existente. Requiere rol **ADMINISTRATOR** o **SUPERVISOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "ID de la categoría a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody UpdateCategoryCommand command
    ) {
        CategoryDto updated = categoryFacade.updateCategory(id, command);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Eliminar categoría",
            description = """
                    Elimina una categoría permanentemente. Solo para **ADMINISTRATOR**.
                    
                    > ⚠️ No se puede eliminar una categoría que tenga productos asociados.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados y no puede eliminarse", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        categoryFacade.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
