package com.sciencebot.pos.billing.internal.adapters;

import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceRequest;
import com.sciencebot.pos.billing.internal.adapters.dto.InvoiceResult;

/**
 * Interfaz Strategy (Puerto de Salida) para proveedores de Facturación Electrónica DIAN.
 * Permite desacoplar el sistema de proveedores externos específicos (Factus, Siigo, Mock, etc.).
 */
public interface ElectronicInvoicingProvider {

    /**
     * Identificador único de la estrategia (ej. "factus", "mock", "siigo").
     */
    String getProviderName();

    /**
     * Emite y valida una factura electrónica ante el proveedor y la entidad tributaria.
     */
    InvoiceResult emitInvoice(InvoiceRequest request);

    /**
     * Consulta el estado de una factura electrónica por su número legal o referencia.
     */
    InvoiceResult queryInvoice(String legalNumber);
}
