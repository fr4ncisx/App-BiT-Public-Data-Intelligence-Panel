package com.appbit.geoanalytics.infrastructure.adapter.in.rest.maps;

import com.appbit.geoanalytics.application.maps.CompareRegionsResponse;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Maps", description = "Datos geoespaciales para visualización en mapa.")
@RequestMapping("/api/v1/maps")
public interface CompareRegionsApi {

    @GetMapping("/compare")
    @Operation(summary = "Comparar indicadores entre dos regiones", description = "Retorna los indicadores de dos regiones lado a lado para comparación directa.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comparación recuperada exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"REGIONS_COMPARED\",\"message\":\"Regions compared successfully\",\"data\":{\"regionA\":{\"regionCode\":\"CBD_BEIRAMAR\",\"regionName\":\"CBD Beiramar\",\"municipality\":\"Florianopolis\",\"concentration\":{\"indicatorType\":\"POPULATION_CONCENTRATION\",\"value\":5000,\"unit\":\"active_users\",\"source\":\"tensor_concentracao.csv\"},\"networkCoverage\":null,\"socialIndicators\":[{\"indicatorType\":\"TRAINING\",\"value\":0.85,\"unit\":\"SCORE\",\"source\":\"social_indicators_seed.csv\"}]},\"regionB\":{\"regionCode\":\"CENTRO\",\"regionName\":\"Centro\",\"municipality\":\"Florianopolis\",\"concentration\":{\"indicatorType\":\"POPULATION_CONCENTRATION\",\"value\":3200,\"unit\":\"active_users\",\"source\":\"tensor_concentracao.csv\"},\"networkCoverage\":null,\"socialIndicators\":[]},\"warnings\":[{\"type\":\"INFO\",\"message\":\"No hay indicadores de red para las regiones seleccionadas.\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada o código de región inválido.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"regionCodeA\",\"reason\":\"Region not found: INVALID_CODE\",\"rejectedValue\":\"INVALID_CODE\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Región no encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Region not found: UNKNOWN\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<CompareRegionsResponse>> compareRegions(
            @RequestParam
            @Parameter(description = "Código de la primera región.", example = "CBD_BEIRAMAR", required = true)
            String regionCodeA,

            @RequestParam
            @Parameter(description = "Código de la segunda región.", example = "CENTRO", required = true)
            String regionCodeB
    );
}
