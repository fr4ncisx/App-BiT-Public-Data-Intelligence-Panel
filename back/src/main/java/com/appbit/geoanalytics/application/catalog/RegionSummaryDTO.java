package com.appbit.geoanalytics.application.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen de una región geográfica para el catálogo.")
public record RegionSummaryDTO(
        @Schema(description = "Código único de la región.", example = "REG_FLORIPA")
        String regionCode,

        @Schema(description = "Nombre de la región.", example = "Florianópolis")
        String regionName,

        @Schema(description = "Municipio al que pertenece.", example = "Florianopolis")
        String municipality
) {
}
