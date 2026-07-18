package com.appbit.geoanalytics.infrastructure.adapter.in.rest.maps;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.application.maps.in.GetRegionsMapUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegionsMapController implements RegionsMapApi {

    private final GetRegionsMapUseCase getRegionsMapUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<RegionsMapResponse>> getRegions(
            @Nullable String indicatorType,
            @Nullable String period,
            @Nullable String regionCode
    ) {
        RegionsMapResponse data = getRegionsMapUseCase.execute(indicatorType, period, regionCode);
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.MAP_REGIONS_RETRIEVED,
                "Map regions retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
