package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Queries", description = "Procesamiento de consultas en lenguaje natural sobre datos territoriales.")
@RequestMapping("/api/v1/data")
public interface QueriesApi {

    @PostMapping("/queries")
    @Operation(summary = "Procesar consulta territorial", description = """
            Clasifica la intención de una consulta en lenguaje natural, recupera evidencia
            de las fuentes disponibles y genera una respuesta estructurada con inteligencia artificial.
            Si la evidencia es insuficiente, retorna un mensaje indicando la limitación.""")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta procesada exitosamente (DATA_QUERY_PROCESSED) o con evidencia insuficiente (INSUFFICIENT_EVIDENCE).",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "DATA_QUERY_PROCESSED", description = "Respuesta completa con evidencia suficiente.",
                                            value = "{\"success\":true,\"code\":\"DATA_QUERY_PROCESSED\",\"message\":\"Query processed successfully\",\"data\":{\"summary\":\"Alta concentración poblacional en Florianópolis.\",\"explanation\":\"Se observan 5000 usuarios activos en la región analizada, lo que indica una alta concentración poblacional en el período MANHA.\",\"data\":[{\"indicatorType\":\"POPULATION_CONCENTRATION\",\"value\":5000,\"unit\":\"USERS\",\"sourceName\":\"Concentración Poblacional\",\"confidenceLevel\":\"HIGH\",\"period\":\"MANHA\"}],\"regions\":[{\"regionCode\":\"REG_FLORIPA\",\"regionName\":\"Florianópolis\",\"municipality\":\"Florianopolis\",\"centerLat\":-27.5954,\"centerLng\":-48.548}],\"sources\":[\"Seed Data\"],\"warnings\":[],\"suggestedVisualization\":\"MAP\"},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"),
                                    @ExampleObject(name = "INSUFFICIENT_EVIDENCE", description = "Respuesta con evidencia insuficiente.",
                                            value = "{\"success\":true,\"code\":\"INSUFFICIENT_EVIDENCE\",\"message\":\"Query processed with insufficient evidence\",\"data\":{\"summary\":\"No hay datos suficientes para responder la consulta.\",\"explanation\":\"Los filtros seleccionados no coinciden con datos disponibles en la base de datos. Intente con una región, indicador o período diferente.\",\"data\":[],\"regions\":[],\"sources\":[],\"warnings\":[{\"type\":\"INFO\",\"message\":\"No hay datos disponibles para los filtros seleccionados.\"}],\"suggestedVisualization\":\"NONE\"},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}")
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación: query vacía, muy larga o mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed for argument\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"query\",\"reason\":\"must not be blank\",\"rejectedValue\":\"\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor o proveedor de IA no disponible.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"AI provider unavailable\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<AIResponseDTO>> query(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Consulta en lenguaje natural con filtros opcionales.", required = true,
                    content = @Content(schema = @Schema(implementation = QueryRequest.class),
                            examples = @ExampleObject(value = "{\"query\":\"concentración poblacional en floripa\",\"regionCode\":\"REG_FLORIPA\",\"indicatorType\":\"TRAINING\",\"period\":\"MANHA\",\"language\":\"pt\"}")))
            QueryRequest request
    );
}
