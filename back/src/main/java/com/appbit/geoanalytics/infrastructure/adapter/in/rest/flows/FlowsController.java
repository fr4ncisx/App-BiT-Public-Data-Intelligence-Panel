package com.appbit.geoanalytics.infrastructure.adapter.in.rest.flows;

import com.appbit.geoanalytics.application.maps.FlowsResponse;
import com.appbit.geoanalytics.application.maps.in.GetFlowsUseCase;
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
public class FlowsController implements FlowsApi {

    private final GetFlowsUseCase getFlowsUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<FlowsResponse>> getFlows() {
        FlowsResponse data = getFlowsUseCase.execute();
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.MAP_REGIONS_RETRIEVED,
                "Flows retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
