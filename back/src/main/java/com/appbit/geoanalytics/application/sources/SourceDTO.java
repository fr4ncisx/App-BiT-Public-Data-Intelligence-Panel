package com.appbit.geoanalytics.application.sources;

import com.appbit.geoanalytics.domain.source.enums.ConfidenceLevel;
import com.appbit.geoanalytics.domain.source.enums.GovernanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Schema(description = "Fuente de datos con su estado de ingestión y clasificación de gobernanza.")
public record SourceDTO(
        @Schema(description = "Nombre de la fuente.", example = "Concentración Poblacional")
        String sourceName,

        @Schema(description = "Nombre del archivo CSV asociado.", example = "concentracion_poblacional.csv")
        String fileName,

        @Schema(description = "Tipo de fuente.", example = "CONCENTRATION")
        String sourceType,

        @Schema(description = "Descripción de la fuente.", example = "Datos de concentración poblacional por periodo")
        String description,

        @Nullable
        @Schema(description = "Nivel de confianza de la fuente.", example = "HIGH", nullable = true)
        ConfidenceLevel confidenceLevel,

        @Nullable
        @Schema(description = "Inicio del período de validez.", example = "MANHA", nullable = true)
        String periodStart,

        @Nullable
        @Schema(description = "Fin del período de validez.", example = "NOITE", nullable = true)
        String periodEnd,

        @Nullable
        @Schema(description = "Tipo de gobernanza de la fuente.", example = "SEED", nullable = true)
        GovernanceType governanceType,

        @Schema(description = "Estado de la última ingestión.", allowableValues = {"COMPLETED", "FAILED", "IN_PROGRESS", "PENDING"}, example = "COMPLETED")
        String lastIngestionState,

        @Nullable
        @Schema(description = "Inicio de la última ingestión.", example = "2025-01-01T00:00:00Z", nullable = true)
        Instant lastIngestionStartedAt,

        @Nullable
        @Schema(description = "Fin de la última ingestión.", example = "2025-01-01T00:05:00Z", nullable = true)
        Instant lastIngestionFinishedAt,

        @Schema(description = "Registros leídos en la última ingestión.", example = "10000")
        long rowsRead,

        @Schema(description = "Registros insertados en la última ingestión.", example = "9500")
        long rowsInserted,

        @Schema(description = "Registros rechazados en la última ingestión.", example = "500")
        long rowsRejected,

        @Nullable
        @Schema(description = "Mensaje de error de la última ingestión (si falló).", example = "Timeout al leer archivo", nullable = true)
        String errorMessage
) {
}
