package com.appbit.geoanalytics.application.social;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Resultado de la consulta de brecha social por tipo de indicador.")
public record SocialGapResponse(
        @Schema(description = "Lista de indicadores de brecha social por región.")
        List<SocialGapItem> indicators,

        @Schema(description = "Advertencias sobre la calidad o procedencia de los datos.")
        List<String> warnings
) {

    @Schema(description = "Indicador de brecha social en una región específica.")
    public record SocialGapItem(
            @Schema(description = "Código único de la región.", example = "CBD_BEIRAMAR")
            String regionCode,

            @Schema(description = "Nombre de la región.", example = "CBD Beiramar")
            String regionName,

            @Schema(description = "Municipio al que pertenece.", example = "Florianopolis")
            String municipality,

            @Schema(description = "Valor del indicador (0-1).", example = "0.75")
            BigDecimal score,

            @Schema(description = "Unidad de medida.", example = "SCORE")
            String unit,

            @Schema(description = "Nivel de brecha.", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL", "UNKNOWN"})
            String gapLevel,

            @Schema(description = "Nivel de confianza.", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
            String confidenceLevel,

            @Schema(description = "Descripción del indicador.", example = "Training programs indicator for CBD Beiramar")
            String description,

            @Schema(description = "Tipo de fuente de datos.", example = "SEED_DATA")
            String sourceType,

            @Schema(description = "Nivel de prioridad calculado por cruce de indicadores.", example = "HIGH")
            String priorityLevel
    ) {
        public SocialGapItem(String regionCode, String regionName, String municipality, BigDecimal score,
                             String unit, String gapLevel, String confidenceLevel, String description, String sourceType) {
            this(regionCode, regionName, municipality, score, unit, gapLevel, confidenceLevel, description, sourceType, null);
        }
    }
}
