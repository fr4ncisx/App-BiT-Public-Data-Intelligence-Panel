package com.appbit.geoanalytics.application.ranking;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Ranking de regiones por tipo de indicador.")
public record RegionRankingResponse(
        @Schema(description = "Lista de regiones rankeadas.")
        List<RankingItem> ranking
) {

    @Schema(description = "Posición de una región en el ranking.")
    public record RankingItem(
            @Schema(description = "Posición en el ranking.", example = "1")
            int position,

            @Schema(description = "Código único de la región.", example = "CBD_BEIRAMAR")
            String regionCode,

            @Schema(description = "Nombre de la región.", example = "CBD Beiramar")
            String regionName,

            @Schema(description = "Municipio.", example = "Florianopolis")
            String municipality,

            @Schema(description = "Valor del indicador (0-1).", example = "0.92")
            BigDecimal score,

            @Schema(description = "Nivel de brecha.", example = "LOW")
            String gapLevel
    ) {
    }
}
