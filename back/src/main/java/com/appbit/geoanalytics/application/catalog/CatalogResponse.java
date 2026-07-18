package com.appbit.geoanalytics.application.catalog;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Catálogo de regiones, indicadores, períodos y fuentes disponibles.")
public record CatalogResponse(
        @Schema(description = "Regiones geográficas disponibles.")
        List<RegionSummaryDTO> regions,

        @Schema(description = "Tipos de indicador disponibles.", example = "[\"TRAINING\", \"EMPLOYABILITY\", \"MENTAL_HEALTH\"]")
        List<String> indicatorTypes,

        @Schema(description = "Períodos disponibles.", example = "[\"MANHA\", \"TARDE\", \"NOITE\"]")
        List<String> periods,

        @Schema(description = "Fuentes de datos disponibles.")
        List<SourceSummaryDTO> sources
) {
}
