package com.sciencebot.pos.billing.internal.adapters;

import com.sciencebot.pos.billing.CreateNumberingRangeRequest;
import com.sciencebot.pos.billing.NumberingRangeDto;

import java.util.List;

/**
 * Puerto de salida (SPI) para administración y sincronización de Rangos de Numeración ante la DIAN y el proveedor.
 */
public interface NumberingRangeProvider {

    /**
     * Identificador del proveedor (ej. "factus", "mock").
     */
    String getProviderName();

    /**
     * Consulta los rangos de numeración asociados ante la DIAN (consulta en vivo).
     */
    default List<NumberingRangeDto> queryDianNumberingRanges() {
        throw new UnsupportedOperationException("queryDianNumberingRanges no soportado por " + getProviderName());
    }

    /**
     * Lista los rangos de numeración registrados en el proveedor.
     */
    default List<NumberingRangeDto> listNumberingRanges() {
        throw new UnsupportedOperationException("listNumberingRanges no soportado por " + getProviderName());
    }

    /**
     * Obtiene el detalle de un rango de numeración por su ID.
     */
    default NumberingRangeDto getNumberingRange(Long numberingRangeId) {
        throw new UnsupportedOperationException("getNumberingRange no soportado por " + getProviderName());
    }

    /**
     * Registra y activa un rango de numeración en el proveedor.
     */
    default NumberingRangeDto createNumberingRange(CreateNumberingRangeRequest request) {
        throw new UnsupportedOperationException("createNumberingRange no soportado por " + getProviderName());
    }

    /**
     * Elimina un rango de numeración en el proveedor.
     */
    default boolean deleteNumberingRange(Long numberingRangeId) {
        throw new UnsupportedOperationException("deleteNumberingRange no soportado por " + getProviderName());
    }

    /**
     * Cambia el estado (activo/inactivo) de un rango de numeración.
     */
    default boolean toggleNumberingRangeStatus(Long numberingRangeId) {
        throw new UnsupportedOperationException("toggleNumberingRangeStatus no soportado por " + getProviderName());
    }
}
