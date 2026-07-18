package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data")
@ConditionalOnProperty(name = "spring.ai.chat.client.enabled", havingValue = "true")
@Tag(name = "Suggestions", description = "Preguntas sugeridas generadas por IA para el panel de consultas.")
public class SuggestionsController {

    private final SuggestedQuestionsService suggestedQuestionsService;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @GetMapping("/suggestions")
    @Operation(summary = "Obtener preguntas sugeridas",
            description = "Retorna 4 preguntas sugeridas pre-generadas por IA en el cold start de la aplicación. " +
                    "Las preguntas se regeneran periódicamente cada 10 minutos.")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions() {
        var suggestions = suggestedQuestionsService.getSuggestions();
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.DATA_QUERY_PROCESSED,
                "Suggested questions retrieved successfully",
                suggestions,
                requestContext.requestId()
        );
    }
}
