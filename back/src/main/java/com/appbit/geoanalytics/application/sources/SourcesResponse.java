package com.appbit.geoanalytics.application.sources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Listado de fuentes de datos disponibles en el sistema.")
public record SourcesResponse(
        @Schema(description = "Fuentes de datos registradas.")
        List<SourceDTO> sources
) {
}
