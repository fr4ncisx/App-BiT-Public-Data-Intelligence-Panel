package com.appbit.geoanalytics.application.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Indicador utilizado como evidencia en la respuesta.")
public record IndicatorEvidenceDTO(
        @Schema(description = "Tipo del indicador.", example = "POPULATION_CONCENTRATION")
        String indicatorType,

        @Schema(description = "Valor del indicador.", example = "5000")
        BigDecimal value,

        @Schema(description = "Unidad de medida del valor.", example = "USERS")
        String unit,

        @Schema(description = "Nombre de la fuente de datos.", example = "Concentración Poblacional")
        String sourceName,

        @Schema(description = "Nivel de confianza del indicador.", allowableValues = {"LOW", "MEDIUM", "HIGH"}, example = "HIGH")
        String confidenceLevel,

        @Schema(description = "Período al que corresponde el indicador.", example = "MANHA", nullable = true)
        String period
) {
}
