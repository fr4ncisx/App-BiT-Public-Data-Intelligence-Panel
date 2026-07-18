package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.application.ai.in.AuditQueryUseCase;
import com.appbit.geoanalytics.application.ai.in.ClassifyIntentUseCase;
import com.appbit.geoanalytics.application.ai.in.GenerateAIAnswerUseCase;
import com.appbit.geoanalytics.application.ai.in.RetrieveEvidenceUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@ConditionalOnBean(GenerateAIAnswerUseCase.class)
public class QueryController implements QueriesApi {

    private final ClassifyIntentUseCase classifyIntentUseCase;
    private final RetrieveEvidenceUseCase retrieveEvidenceUseCase;
    private final GenerateAIAnswerUseCase generateAIAnswerUseCase;
    private final AuditQueryUseCase auditQueryUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<AIResponseDTO>> query(
            @Valid QueryRequest request
    ) {
        var intent = classifyIntentUseCase.execute(request.query());
        var evidence = retrieveEvidenceUseCase.execute(
                intent, request.regionCode(), request.indicatorType(), request.period());
        var language = parseLanguage(request.language());
        var response = generateAIAnswerUseCase.execute(evidence, intent, language);

        var code = evidence.isEvidenceSufficient()
                ? ApiResponseCode.DATA_QUERY_PROCESSED
                : ApiResponseCode.INSUFFICIENT_EVIDENCE;

        var auditStatus = evidence.isEvidenceSufficient() ? "PROCESSED" : "INSUFFICIENT_EVIDENCE";
        auditQueryUseCase.execute(request, intent, response, requestContext.requestId(), auditStatus);

        return responseFactory.success(
                HttpStatus.OK,
                code,
                code == ApiResponseCode.DATA_QUERY_PROCESSED
                        ? "Query processed successfully"
                        : "Query processed with insufficient evidence",
                response,
                requestContext.requestId()
        );
    }

    private static @Nullable Language parseLanguage(@Nullable String language) {
        if (language == null || language.isBlank()) return null;
        try {
            return Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }
}
