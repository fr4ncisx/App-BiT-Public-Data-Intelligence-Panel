package com.appbit.geoanalytics.application.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Región geográfica incluida en la evidencia.")
public record RegionEvidenceDTO(
        @Schema(description = "Código único de la región.", example = "REG_FLORIPA")
        String regionCode,

        @Schema(description = "Nombre de la región.", example = "Florianópolis")
        String regionName,

        @Schema(description = "Municipio al que pertenece.", example = "Florianopolis")
        String municipality,

        @Schema(description = "Latitud del centro geográfico.", example = "-27.5954")
        BigDecimal centerLat,

        @Schema(description = "Longitud del centro geográfico.", example = "-48.5480")
        BigDecimal centerLng
) {
}
