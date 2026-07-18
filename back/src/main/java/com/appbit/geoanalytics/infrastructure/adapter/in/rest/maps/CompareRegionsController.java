package com.appbit.geoanalytics.infrastructure.adapter.in.rest.maps;

import com.appbit.geoanalytics.application.maps.CompareRegionsResponse;
import com.appbit.geoanalytics.application.maps.in.CompareRegionsUseCase;
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
public class CompareRegionsController implements CompareRegionsApi {

    private final CompareRegionsUseCase compareRegionsUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<CompareRegionsResponse>> compareRegions(String regionCodeA, String regionCodeB) {
        CompareRegionsResponse data = compareRegionsUseCase.execute(regionCodeA, regionCodeB);
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.REGIONS_COMPARED,
                "Regions compared successfully",
                data,
                requestContext.requestId()
        );
    }
}
