package com.appbit.geoanalytics.infrastructure.adapter.in.rest.catalog;

import com.appbit.geoanalytics.application.catalog.CatalogResponse;
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

@Tag(name = "Catalog", description = "Consulta del catálogo de regiones, indicadores, períodos y fuentes.")
@RequestMapping("/api/v1/data")
public interface CatalogApi {

    @GetMapping("/catalog")
    @Operation(summary = "Obtener catálogo", description = "Retorna el catálogo completo con regiones, tipos de indicador, períodos y fuentes disponibles.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Catálogo recuperado exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"CATALOG_RETRIEVED\",\"message\":\"Catalog retrieved successfully\",\"data\":{\"regions\":[{\"regionCode\":\"REG_FLORIPA\",\"regionName\":\"Florianópolis\",\"municipality\":\"Florianopolis\"}],\"indicatorTypes\":[\"TRAINING\",\"EMPLOYABILITY\",\"MENTAL_HEALTH\",\"CONNECTIVITY\"],\"periods\":[\"MANHA\",\"TARDE\",\"NOITE\"],\"sources\":[{\"name\":\"Concentración Poblacional\",\"file\":\"concentracion_poblacional.csv\",\"sourceType\":\"CONCENTRATION\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada o error de validación.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"indicatorType\",\"reason\":\"Invalid indicator type\",\"rejectedValue\":\"INVALID\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Catalog not found\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<CatalogResponse>> getCatalog();
}
