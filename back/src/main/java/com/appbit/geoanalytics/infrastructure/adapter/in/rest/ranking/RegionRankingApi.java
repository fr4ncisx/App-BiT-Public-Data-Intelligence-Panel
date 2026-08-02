package com.appbit.geoanalytics.infrastructure.adapter.in.rest.ranking;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Ranking", description = "Ranking de regiones por tipo de indicador social.")
@RequestMapping("/api/v1/ranking")
public interface RegionRankingApi {

    @GetMapping
    @Operation(summary = "Obtener ranking de regiones", description = "Retorna las regiones ordenadas por valor del indicador (descendente), filtradas por tipo de indicador.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking recuperado exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"RANKING_RETRIEVED\",\"message\":\"Region ranking retrieved successfully\",\"data\":{\"ranking\":[{\"position\":1,\"regionCode\":\"CBD_BEIRAMAR\",\"regionName\":\"CBD Beiramar\",\"municipality\":\"Florianopolis\",\"score\":0.92,\"gapLevel\":\"LOW\"},{\"position\":2,\"regionCode\":\"INGLESES\",\"regionName\":\"Ingleses\",\"municipality\":\"Florianopolis\",\"score\":0.82,\"gapLevel\":\"LOW\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":\"indicatorType\",\"reason\":\"Invalid indicator type\",\"rejectedValue\":\"INVALID\"}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<RegionRankingResponse>> getRanking(
            @RequestParam
            @Parameter(description = "Tipo de indicador para el ranking.", example = "TRAINING", required = true)
            String indicatorType,

            @RequestParam(defaultValue = "10") @Min(1) @Max(1000)
            @Parameter(description = "Cantidad máxima de regiones en el resultado.", example = "10")
            @Nullable
            Integer limit
    );
}
