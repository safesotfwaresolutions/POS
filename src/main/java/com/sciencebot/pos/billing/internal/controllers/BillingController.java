package com.sciencebot.pos.billing.internal.controllers;

import com.sciencebot.pos.billing.BillingFacade;
import com.sciencebot.pos.billing.ElectronicInvoiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@Tag(name = "📄 Facturación Electrónica", description = "Integración con Factus para facturación electrónica DIAN")
public class BillingController {

    private final BillingFacade billingFacade;

    public BillingController(BillingFacade billingFacade) {
        this.billingFacade = billingFacade;
    }

    @GetMapping("/sales/{saleId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Consultar factura electrónica de una venta",
            description = """
                    Retorna el estado y datos de la factura electrónica generada para una venta específica.
                    
                    **Campos clave:**
                    - `status`: `PENDING`, `VALIDATED`, `ERROR`
                    - `cufe`: Código Único de Factura Electrónica (DIAN)
                    - `pdfUrl`: URL al PDF de la factura en Factus
                    - `qrCode`: Código QR para validación DIAN
                    - `errorMessage`: Detalle del error si el status es `ERROR`
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Factura electrónica encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ElectronicInvoiceDto.class))),
            @ApiResponse(responseCode = "404", description = "No existe factura electrónica para esta venta", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    public ResponseEntity<ElectronicInvoiceDto> getInvoiceBySaleId(
            @Parameter(description = "ID de la venta", example = "1", required = true)
            @PathVariable Long saleId) {
        return billingFacade.getBySaleId(saleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sales/{saleId}/retry")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR')")
    @Operation(
            summary = "Reintentar facturación electrónica",
            description = """
                    Reintenta el envío de la factura electrónica a Factus para una venta que haya fallado (`status = ERROR`).
                    
                    Útil cuando:
                    - Hubo un error de conexión con Factus durante la venta
                    - La factura fue rechazada por errores de validación DIAN
                    
                    Requiere rol **ADMINISTRATOR** o **SUPERVISOR**.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reintento procesado — retorna el estado actualizado de la factura",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ElectronicInvoiceDto.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "La factura ya está validada y no puede reintentarse", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content)
    })
    public ResponseEntity<ElectronicInvoiceDto> retryInvoice(
            @Parameter(description = "ID de la venta a re-facturar", example = "1", required = true)
            @PathVariable Long saleId) {
        ElectronicInvoiceDto result = billingFacade.retryInvoice(saleId);
        return ResponseEntity.ok(result);
    }
}
