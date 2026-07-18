package com.appbit.geoanalytics.application.maps;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Datos de regiones geográficas para visualización en mapa.")
public record RegionsMapResponse(
        @Schema(description = "Regiones con sus indicadores y coordenadas.")
        List<RegionMapDTO> regions
) {

    @Schema(description = "Región geográfica con indicadores para visualización en mapa.")
    public record RegionMapDTO(
            @Schema(description = "Identificador único de la región.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            UUID id,

            @Schema(description = "Código único de la región.", example = "CBD_BEIRAMAR")
            String regionCode,

            @Schema(description = "Nombre de la región.", example = "CBD Beiramar")
            String regionName,

            @Schema(description = "Municipio al que pertenece.", example = "Florianopolis")
            String municipality,

            @Schema(description = "Coordenadas geográficas del centro de la región.")
            GeoPointDTO geoPoint,

            @Nullable
            @Schema(description = "Indicadores asociados a la región.", nullable = true)
            RegionIndicatorsDTO indicators
    ) {
    }

    @Schema(description = "Punto geográfico con latitud y longitud.")
    public record GeoPointDTO(
            @Schema(description = "Latitud.", example = "-27.5954")
            BigDecimal lat,

            @Schema(description = "Longitud.", example = "-48.5480")
            BigDecimal lng
    ) {
    }

    @Schema(description = "Indicadores disponibles para una región.")
    public record RegionIndicatorsDTO(
            @Nullable
            @Schema(description = "Indicador de concentración poblacional.", nullable = true)
            IndicatorDetailDTO populationConcentration,

            @Nullable
            @Schema(description = "Indicador de cobertura de red.", nullable = true)
            IndicatorDetailDTO networkCoverage,

            @Nullable
            @Schema(description = "Indicador de programas de formación.", nullable = true)
            IndicatorDetailDTO trainingPrograms
    ) {
    }

    @Schema(description = "Detalle de un indicador con valor, unidad y fuente.")
    public record IndicatorDetailDTO(
            @Schema(description = "Valor del indicador.", example = "85.5")
            BigDecimal value,

            @Schema(description = "Unidad de medida.", example = "SCORE")
            String unit,

            @Schema(description = "Fuente del indicador.", example = "Seed Data")
            String source
    ) {
    }
}
