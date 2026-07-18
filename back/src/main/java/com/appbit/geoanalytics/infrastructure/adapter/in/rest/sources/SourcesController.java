package com.appbit.geoanalytics.infrastructure.adapter.in.rest.sources;

import com.appbit.geoanalytics.application.sources.SourcesResponse;
import com.appbit.geoanalytics.application.sources.in.GetSourcesUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SourcesController implements SourcesApi {

    private final GetSourcesUseCase getSourcesUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<SourcesResponse>> getSources() {
        SourcesResponse data = getSourcesUseCase.execute();
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.SOURCES_RETRIEVED,
                "Sources retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
