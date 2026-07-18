package com.appbit.geoanalytics.infrastructure.adapter.in.rest.maps;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Maps", description = "Datos geoespaciales para visualización en mapa.")
@RequestMapping("/api/v1/maps")
public interface RegionsMapApi {

    @GetMapping("/regions")
    @Operation(summary = "Obtener regiones para mapa", description = "Retorna las regiones geográficas con sus indicadores y coordenadas, opcionalmente filtradas por tipo de indicador, período o código de región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Regiones del mapa recuperadas exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"MAP_REGIONS_RETRIEVED\",\"message\":\"Map regions retrieved successfully\",\"data\":{\"regions\":[{\"id\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"regionCode\":\"CBD_BEIRAMAR\",\"regionName\":\"CBD Beiramar\",\"municipality\":\"Florianopolis\",\"geoPoint\":{\"lat\":-27.5954,\"lng\":-48.548},\"indicators\":{\"populationConcentration\":{\"value\":85.5,\"unit\":\"SCORE\",\"source\":\"Seed Data\"},\"networkCoverage\":null,\"trainingPrograms\":null}}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada o error de validación.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"indicatorType\",\"reason\":\"Invalid indicator type\",\"rejectedValue\":\"INVALID\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Region not found\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<RegionsMapResponse>> getRegions(
            @RequestParam @Nullable
            @Parameter(description = "Filtrar por tipo de indicador.", example = "TRAINING")
            String indicatorType,

            @RequestParam @Nullable
            @Parameter(description = "Filtrar por período.", example = "MANHA")
            String period,

            @RequestParam @Nullable
            @Parameter(description = "Filtrar por código de región.", example = "CBD_BEIRAMAR")
            String regionCode
    );
}
