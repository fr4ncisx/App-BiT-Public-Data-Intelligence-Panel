package com.appbit.geoanalytics.application.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de una fuente de datos para el catálogo.")
public record SourceSummaryDTO(
        @Schema(description = "Nombre de la fuente.", example = "Concentración Poblacional")
        String name,

        @Schema(description = "Nombre del archivo.", example = "concentracion_poblacional.csv")
        String file,

        @Schema(description = "Tipo de fuente.", example = "CONCENTRATION")
        String sourceType
) {
}
