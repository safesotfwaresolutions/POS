package com.sciencebot.pos.suppliers.internal.controllers;

import com.sciencebot.pos.suppliers.CreateSupplierCommand;
import com.sciencebot.pos.suppliers.SupplierDto;
import com.sciencebot.pos.suppliers.SupplierFacade;
import com.sciencebot.pos.suppliers.UpdateSupplierCommand;
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

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "🏭 Proveedores", description = "Gestión de proveedores de la empresa")
public class SupplierController {

    private final SupplierFacade supplierFacade;

    public SupplierController(SupplierFacade supplierFacade) {
        this.supplierFacade = supplierFacade;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Buscar proveedores",
            description = "Lista y filtra proveedores con paginación. El `search` busca por nombre de empresa, NIT o nombre de contacto."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de proveedores",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<Page<SupplierDto>> searchSuppliers(
            @Parameter(description = "Búsqueda por nombre de empresa, NIT o contacto", example = "Distribuidora Central")
            @RequestParam(required = false) String search,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierDto> suppliers = supplierFacade.searchSuppliers(search, pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Obtener proveedor por ID",
            description = "Retorna el detalle completo de un proveedor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<SupplierDto> getSupplierById(
            @Parameter(description = "ID del proveedor", example = "1", required = true)
            @PathVariable Long id) {
        return supplierFacade.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Crear proveedor",
            description = "Registra un nuevo proveedor. Requiere rol **ADMINISTRATOR** o **SUPERVISOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o NIT duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<SupplierDto> createSupplier(@RequestBody CreateSupplierCommand command) {
        SupplierDto created = supplierFacade.createSupplier(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza los datos de un proveedor existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierDto.class))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<SupplierDto> updateSupplier(
            @Parameter(description = "ID del proveedor a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody UpdateSupplierCommand command
    ) {
        SupplierDto updated = supplierFacade.updateSupplier(id, command);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Eliminar proveedor",
            description = "Elimina permanentemente un proveedor. Solo para **ADMINISTRATOR**."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "El proveedor tiene compras asociadas y no puede eliminarse", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "ID del proveedor a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        supplierFacade.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
