package com.sciencebot.pos.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNumberingRangeRequest(
        @Schema(description = "Código de documento (ej: '01' para Factura Electrónica de Venta)", example = "01")
        @NotBlank(message = "El código de documento es obligatorio")
        String document,

        @Schema(description = "Prefijo alfanumérico del rango de numeración", example = "SETP")
        String prefix,

        @Schema(description = "Número de resolución del rango de numeración en la DIAN", example = "18764000001234")
        @NotBlank(message = "El número de resolución es obligatorio")
        @JsonProperty("resolution_number")
        String resolutionNumber,

        @Schema(description = "Número actual del consecutivo (inicio de numeración)", example = "1")
        @NotNull(message = "El consecutivo actual es obligatorio")
        Long current,

        @Schema(description = "Fecha inicio de la resolución (YYYY-MM-DD)", example = "2026-01-01")
        @JsonProperty("start_date")
        String startDate,

        @Schema(description = "Fecha fin de la resolución (YYYY-MM-DD)", example = "2027-01-01")
        @JsonProperty("end_date")
        String endDate,

        @Schema(description = "Número desde", example = "1")
        Long from,

        @Schema(description = "Número hasta", example = "5000")
        Long to,

        @Schema(description = "Clave técnica asignada por la DIAN", example = "fc8eac422eba16e22ffd8c6f94b3f40a6e381edf")
        @JsonProperty("technical_key")
        String technicalKey
) {}