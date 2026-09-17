package com.sciencebot.pos.billing;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NumberingRangeDto(
        @Schema(description = "ID del rango de numeración en Factus", example = "14")
        Long id,

        @Schema(description = "Código de documento", example = "01")
        String document,

        @Schema(description = "Prefijo alfanumérico", example = "SETP")
        String prefix,

        @Schema(description = "Número de resolución DIAN", example = "18764000001234")
        String resolutionNumber,

        @Schema(description = "Consecutivo desde", example = "1")
        Long from,

        @Schema(description = "Consecutivo hasta", example = "5000")
        Long to,

        @Schema(description = "Consecutivo actual", example = "1")
        Long current,

        @Schema(description = "Fecha de inicio de vigencia", example = "2026-01-01")
        String startDate,

        @Schema(description = "Fecha de fin de vigencia", example = "2027-01-01")
        String endDate,

        @Schema(description = "Clave técnica DIAN", example = "fc8eac422eba16e22ffd8c6f94b3f40a6e381edf")
        String technicalKey,

        @Schema(description = "Indica si el rango está activo en Factus", example = "true")
        Boolean isActive
) {}

