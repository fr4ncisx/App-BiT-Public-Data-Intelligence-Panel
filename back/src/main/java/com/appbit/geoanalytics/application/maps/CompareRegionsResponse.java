package com.appbit.geoanalytics.application.maps;

import com.appbit.geoanalytics.application.ai.WarningDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Resultado de la comparación entre dos regiones.")
public record CompareRegionsResponse(
        @Schema(description = "Indicadores de la primera región.")
        RegionCompareDTO regionA,

        @Schema(description = "Indicadores de la segunda región.")
        RegionCompareDTO regionB,

        @Schema(description = "Advertencias sobre calidad o limitaciones de los datos.")
        List<WarningDTO> warnings
) {

    @Schema(description = "Indicadores de una región para su comparación.")
    public record RegionCompareDTO(
            @Schema(description = "Código único de la región.", example = "CBD_BEIRAMAR")
            String regionCode,

            @Schema(description = "Nombre de la región.", example = "CBD Beiramar")
            String regionName,

            @Schema(description = "Municipio al que pertenece.", example = "Florianopolis")
            String municipality,

            @Nullable
            @Schema(description = "Indicador de concentración poblacional.", nullable = true)
            IndicatorDetailDTO concentration,

            @Nullable
            @Schema(description = "Indicador de cobertura de red.", nullable = true)
            IndicatorDetailDTO networkCoverage,

            @Schema(description = "Indicadores de brecha social.")
            List<IndicatorDetailDTO> socialIndicators
    ) {
    }

    @Schema(description = "Detalle de un indicador con tipo, valor, unidad y fuente.")
    public record IndicatorDetailDTO(
            @Schema(description = "Tipo del indicador.", example = "POPULATION_CONCENTRATION")
            String indicatorType,

            @Schema(description = "Valor del indicador.", example = "85.5")
            BigDecimal value,

            @Schema(description = "Unidad de medida.", example = "SCORE")
            String unit,

            @Schema(description = "Fuente del indicador.", example = "tensor_concentracao.csv")
            String source
    ) {
    }
}
