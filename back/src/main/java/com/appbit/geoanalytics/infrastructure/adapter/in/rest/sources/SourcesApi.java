package com.appbit.geoanalytics.infrastructure.adapter.in.rest.sources;

import com.appbit.geoanalytics.application.sources.SourcesResponse;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Sources", description = "Consulta de fuentes de datos disponibles en el sistema.")
@RequestMapping("/api/v1/data")
public interface SourcesApi {

    @GetMapping("/sources")
    @Operation(summary = "Listar fuentes de datos", description = "Retorna todas las fuentes de datos registradas con su estado de ingestión más reciente.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuentes recuperadas exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOURCES_RETRIEVED\",\"message\":\"Sources retrieved successfully\",\"data\":{\"sources\":[{\"sourceName\":\"Concentración Poblacional\",\"fileName\":\"concentracion_poblacional.csv\",\"sourceType\":\"CONCENTRATION\",\"description\":\"Datos de concentración poblacional por periodo\",\"lastIngestionState\":\"COMPLETED\",\"lastIngestionStartedAt\":\"2025-06-01T10:00:00Z\",\"lastIngestionFinishedAt\":\"2025-06-01T10:05:30Z\",\"rowsRead\":15000,\"rowsInserted\":14200,\"rowsRejected\":800,\"errorMessage\":null}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada o error de validación.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"sourceType\",\"reason\":\"Invalid source type\",\"rejectedValue\":\"INVALID\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Source not found\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SourcesResponse>> getSources();
}
