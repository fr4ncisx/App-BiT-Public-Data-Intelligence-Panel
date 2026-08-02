package com.appbit.geoanalytics.infrastructure.adapter.in.rest.social;

import com.appbit.geoanalytics.application.social.SocialGapResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Social", description = "Indicadores de brecha social por tipo.")
@RequestMapping("/api/v1/social/gap")
public interface SocialGapApi {

    @GetMapping("/training")
    @Operation(summary = "Obtener brecha de formación", description = "Retorna los indicadores de brecha de formación (training gap) por región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicadores recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOCIAL_GAP_RETRIEVED\",\"message\":\"Social gap indicators retrieved successfully\",\"data\":{\"indicators\":[{\"regionCode\":\"CBD_BEIRAMAR\",\"regionName\":\"CBD Beiramar\",\"municipality\":\"Florianopolis\",\"score\":0.75,\"unit\":\"SCORE\",\"gapLevel\":\"HIGH\",\"confidenceLevel\":\"MEDIUM\",\"description\":\"Training programs indicator for CBD Beiramar\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Invalid indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"No data found for indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SocialGapResponse>> getTrainingGap(
            @Parameter(description = "Número máximo de resultados a retornar.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int limit,
            @Parameter(description = "Número de resultados a saltar para paginación.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset
    );

    @GetMapping("/employability")
    @Operation(summary = "Obtener brecha de empleabilidad", description = "Retorna los indicadores de brecha de empleabilidad (employability gap) por región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicadores recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOCIAL_GAP_RETRIEVED\",\"message\":\"Social gap indicators retrieved successfully\",\"data\":{\"indicators\":[{\"regionCode\":\"CENTRO\",\"regionName\":\"Centro\",\"municipality\":\"Florianopolis\",\"score\":0.60,\"unit\":\"SCORE\",\"gapLevel\":\"MEDIUM\",\"confidenceLevel\":\"HIGH\",\"description\":\"Employability indicator for Centro\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Invalid indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"No data found for indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SocialGapResponse>> getEmployabilityGap(
            @Parameter(description = "Número máximo de resultados a retornar.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int limit,
            @Parameter(description = "Número de resultados a saltar para paginación.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset
    );

    @GetMapping("/mental-health")
    @Operation(summary = "Obtener brecha de salud mental", description = "Retorna los indicadores de acceso a salud mental (mental health) por región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicadores recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOCIAL_GAP_RETRIEVED\",\"message\":\"Social gap indicators retrieved successfully\",\"data\":{\"indicators\":[{\"regionCode\":\"TRINDADE\",\"regionName\":\"Trindade\",\"municipality\":\"Florianopolis\",\"score\":0.45,\"unit\":\"SCORE\",\"gapLevel\":\"CRITICAL\",\"confidenceLevel\":\"MEDIUM\",\"description\":\"Mental health access indicator for Trindade\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Invalid indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"No data found for indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SocialGapResponse>> getMentalHealthGap(
            @Parameter(description = "Número máximo de resultados a retornar.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int limit,
            @Parameter(description = "Número de resultados a saltar para paginación.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset
    );

    @GetMapping("/mentorship")
    @Operation(summary = "Obtener necesidad de mentoría", description = "Retorna los indicadores de necesidad de mentoría (mentorship need) por región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicadores recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOCIAL_GAP_RETRIEVED\",\"message\":\"Social gap indicators retrieved successfully\",\"data\":{\"indicators\":[{\"regionCode\":\"INGLESES\",\"regionName\":\"Ingleses\",\"municipality\":\"Florianopolis\",\"score\":0.82,\"unit\":\"SCORE\",\"gapLevel\":\"LOW\",\"confidenceLevel\":\"HIGH\",\"description\":\"Mentorship need indicator for Ingleses\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Invalid indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"No data found for indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SocialGapResponse>> getMentorshipGap(
            @Parameter(description = "Número máximo de resultados a retornar.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int limit,
            @Parameter(description = "Número de resultados a saltar para paginación.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset
    );

    @GetMapping("/social-experience")
    @Operation(summary = "Obtener experiencia social", description = "Retorna los indicadores de experiencia social (social experience) por región.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Indicadores recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"SOCIAL_GAP_RETRIEVED\",\"message\":\"Social gap indicators retrieved successfully\",\"data\":{\"indicators\":[{\"regionCode\":\"LAGOA\",\"regionName\":\"Lagoa\",\"municipality\":\"Florianopolis\",\"score\":0.55,\"unit\":\"SCORE\",\"gapLevel\":\"MEDIUM\",\"confidenceLevel\":\"LOW\",\"description\":\"Social experience indicator for Lagoa\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud mal formada.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Invalid indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"Resource not found\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"No data found for indicator type\",\"rejectedValue\":null}]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<SocialGapResponse>> getSocialExperienceGap(
            @Parameter(description = "Número máximo de resultados a retornar.", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int limit,
            @Parameter(description = "Número de resultados a saltar para paginación.", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset
    );
}
