package com.appbit.geoanalytics.application.catalog.in;

import com.appbit.geoanalytics.application.catalog.CatalogResponse;
import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;
import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;
import com.appbit.geoanalytics.application.catalog.out.IndicatorTypeCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.PeriodCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.RegionCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.SourceCatalogPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetCatalogService implements GetCatalogUseCase {

    private final RegionCatalogPort regionCatalogPort;
    private final SourceCatalogPort sourceCatalogPort;
    private final PeriodCatalogPort periodCatalogPort;
    private final IndicatorTypeCatalogPort indicatorTypeCatalogPort;

    @Override
    public CatalogResponse execute() {
        List<RegionSummaryDTO> regions = regionCatalogPort.findAll();
        List<SourceSummaryDTO> sources = sourceCatalogPort.findAll();
        List<String> periods = periodCatalogPort.findAll();
        List<String> indicatorTypes = indicatorTypeCatalogPort.findAll();

        return new CatalogResponse(regions, indicatorTypes, periods, sources);
    }
}
