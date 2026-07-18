package com.appbit.geoanalytics.application.ai;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta generada por IA para una consulta territorial.")
public record AIResponseDTO(
        @Schema(description = "Resumen breve de la respuesta (1-2 oraciones).", example = "Alta concentración poblacional en Florianópolis.")
        String summary,

        @Schema(description = "Explicación detallada basada en los datos disponibles.", example = "Se observan 5000 usuarios activos en la región analizada.")
        String explanation,

        @Schema(description = "Indicadores utilizados como evidencia.")
        List<IndicatorEvidenceDTO> data,

        @Schema(description = "Regiones involucradas en la respuesta.")
        List<RegionEvidenceDTO> regions,

        @Schema(description = "Fuentes de datos consultadas.", example = "[\"Seed Data\"]")
        List<String> sources,

        @Schema(description = "Advertencias sobre limitaciones o calidad de los datos.")
        List<WarningDTO> warnings,

        @Schema(description = "Visualización sugerida para los resultados.", allowableValues = {"MAP", "TABLE", "RANKING", "FLOW", "NONE"}, example = "MAP")
        String suggestedVisualization
) {
}
