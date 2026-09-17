package com.sciencebot.pos.billing.internal.controllers;

import com.sciencebot.pos.billing.ElectronicInvoiceDto;
import com.sciencebot.pos.billing.SendInvoiceEmailRequest;
import com.sciencebot.pos.billing.internal.services.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing/sales")
@Tag(name = "📄 Facturación Electrónica - Facturas", description = "Emisión, consulta y reintento de facturas electrónicas de venta ante la DIAN / Factus")
public class BillingInvoiceController {

    private final InvoiceService invoiceService;

    public BillingInvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{saleId}")
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
        return invoiceService.getBySaleId(saleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{saleId}/retry")
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
        ElectronicInvoiceDto result = invoiceService.retryInvoice(saleId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{saleId}/send-email")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SUPERVISOR', 'SELLER')")
    @Operation(
            summary = "Enviar factura electrónica por correo electrónico",
            description = """
                    Envía el archivo ZIP con el PDF y XML legal de la factura electrónica
                    al correo electrónico indicado en el cuerpo de la petición.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo enviado exitosamente"),
            @ApiResponse(responseCode = "400", description = "La factura no ha sido validada por la DIAN o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Venta o factura no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> sendInvoiceEmail(
            @Parameter(description = "ID de la venta cuya factura se enviará", example = "1", required = true)
            @PathVariable Long saleId,
            @Valid @RequestBody SendInvoiceEmailRequest request) {
        invoiceService.sendInvoiceEmail(saleId, request.email());
        return ResponseEntity.ok().build();
    }
}
