package com.appbit.geoanalytics.application.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

@Schema(description = "Solicitud de consulta en lenguaje natural para análisis territorial.")
public record QueryRequest(
        @NotBlank @Size(max = 500)
        @Schema(description = "Consulta en lenguaje natural (máx. 500 caracteres).", example = "concentración poblacional en floripa")
        String query,

        @Nullable
        @Size(max = 80)
        @Schema(description = "Código de región para filtrar (opcional).", example = "REG_FLORIPA", nullable = true)
        String regionCode,

        @Nullable
        @Size(max = 80)
        @Schema(description = "Tipo de indicador para filtrar (opcional).", example = "TRAINING", nullable = true)
        String indicatorType,

        @Nullable
        @Size(max = 80)
        @Schema(description = "Período para filtrar (opcional).", example = "MANHA", nullable = true)
        String period,

        @Nullable
        @Size(max = 16)
        @Schema(description = "Idioma de la consulta (opcional).", example = "pt", nullable = true)
        String language
) {
}
