package com.sciencebot.pos.sales.internal.controllers;

import com.sciencebot.pos.sales.CreateSaleCommand;
import com.sciencebot.pos.sales.SaleDto;
import com.sciencebot.pos.sales.SaleFacade;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "💰 Ventas", description = "Registro y consulta de transacciones de venta")
public class SaleController {

    private final SaleFacade saleFacade;

    public SaleController(SaleFacade saleFacade) {
        this.saleFacade = saleFacade;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Registrar venta",
            description = """
                    Crea una nueva venta en el sistema. Proceso:
                    1. Valida stock disponible para cada ítem
                    2. Descuenta inventario automáticamente
                    3. Genera número de factura interno
                    4. Dispara el proceso de facturación electrónica con Factus (asíncrono)
                    
                    El `customerId` es opcional — si no se envía, la venta se registra como venta a consumidor final.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SaleDto.class))),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<SaleDto> registerSale(@RequestBody CreateSaleCommand command) {
        SaleDto created = saleFacade.registerSale(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Buscar ventas",
            description = """
                    Lista ventas con filtros opcionales. Soporta paginación.
                    - `customerId` — filtra por cliente específico
                    - `dateFrom` y `dateTo` — rango de fechas en formato ISO 8601: `2025-01-15T00:00:00`
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ventas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<Page<SaleDto>> searchSales(
            @Parameter(description = "Filtrar por ID de cliente", example = "3")
            @RequestParam(required = false) Long customerId,
            @Parameter(description = "Fecha inicial del rango (ISO 8601)", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Fecha final del rango (ISO 8601)", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SaleDto> sales = saleFacade.searchSales(customerId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Obtener venta por ID",
            description = "Retorna el detalle completo de una venta incluyendo todos sus ítems."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SaleDto.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<SaleDto> getSaleById(
            @Parameter(description = "ID de la venta", example = "1", required = true)
            @PathVariable Long id) {
        return saleFacade.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
