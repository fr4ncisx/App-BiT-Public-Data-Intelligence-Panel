package com.appbit.geoanalytics.application.ai;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Advertencia sobre limitaciones o calidad de los datos.")
public record WarningDTO(
        @Schema(description = "Nivel de la advertencia.", allowableValues = {"INFO", "WARNING", "ERROR"}, example = "WARNING")
        String type,

        @Schema(description = "Mensaje descriptivo de la advertencia.", example = "Los indicadores provienen de datos semilla estimados.")
        String message
) {
}
