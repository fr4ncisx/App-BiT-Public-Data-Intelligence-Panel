package com.appbit.geoanalytics.infrastructure.adapter.in.rest.ranking;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;
import com.appbit.geoanalytics.application.ranking.in.GetRegionRankingUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class RegionRankingController implements RegionRankingApi {

    private final GetRegionRankingUseCase getRegionRankingUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<RegionRankingResponse>> getRanking(String indicatorType, @Nullable Integer limit) {
        RegionRankingResponse data = getRegionRankingUseCase.execute(indicatorType, limit != null ? limit : 10);
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.RANKING_RETRIEVED,
                "Region ranking retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
