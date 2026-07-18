package com.appbit.geoanalytics.infrastructure.adapter.in.rest.catalog;

import com.appbit.geoanalytics.application.catalog.CatalogResponse;
import com.appbit.geoanalytics.application.catalog.in.GetCatalogUseCase;
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
public class CatalogController implements CatalogApi {

    private final GetCatalogUseCase getCatalogUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<CatalogResponse>> getCatalog() {
        CatalogResponse data = getCatalogUseCase.execute();
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.CATALOG_RETRIEVED,
                "Catalog retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
