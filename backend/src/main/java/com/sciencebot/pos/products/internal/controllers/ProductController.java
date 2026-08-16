package com.sciencebot.pos.products.internal.controllers;

import com.sciencebot.pos.products.CreateProductCommand;
import com.sciencebot.pos.products.ProductDto;
import com.sciencebot.pos.products.ProductFacade;
import com.sciencebot.pos.products.UpdateProductCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "📦 Productos", description = "Catálogo de productos, precios y disponibilidad")
public class ProductController {

    private final ProductFacade productFacade;

    public ProductController(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Buscar productos",
            description = """
                    Busca y lista productos con soporte de paginación y filtros opcionales.
                    - `search` — busca por nombre, código interno o código de barras (búsqueda parcial)
                    - `categoryId` — filtra por categoría
                    - `active` — filtra por estado (`true` = activos, `false` = inactivos)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @Parameter(description = "Texto de búsqueda (nombre, código interno, código de barras)", example = "Arroz")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por ID de categoría", example = "2")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filtrar por estado activo", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> products = productFacade.searchProducts(search, categoryId, active, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Retorna el detalle completo de un producto específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id) {
        return productFacade.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Crear producto",
            description = "Registra un nuevo producto en el catálogo. Requiere rol **ADMINISTRATOR** o **SUPERVISOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductCommand command) {
        ProductDto created = productFacade.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza todos los campos de un producto existente. Requiere rol **ADMINISTRATOR** o **SUPERVISOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "ID del producto a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody UpdateProductCommand command
    ) {
        ProductDto updated = productFacade.updateProduct(id, command);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Cambiar estado del producto",
            description = """
                    Activa o desactiva un producto sin eliminarlo del sistema.
                    Un producto inactivo no aparece en el punto de venta.
                    
                    **Cuerpo requerido:** `{ "active": true }` o `{ "active": false }`
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estado cambiado correctamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Campo 'active' requerido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<Void> changeStatus(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Estado a asignar al producto",
                    content = @Content(schema = @Schema(example = "{\"active\": true}"))
            )
            @RequestBody Map<String, Boolean> statusMap
    ) {
        Boolean active = statusMap.get("active");
        if (active == null) {
            throw new IllegalArgumentException("El campo 'active' es requerido");
        }
        productFacade.changeStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Eliminar producto",
            description = "Elimina permanentemente un producto del catálogo. Solo accesible para **ADMINISTRATOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo ADMINISTRATOR puede eliminar productos", content = @Content)
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        productFacade.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
