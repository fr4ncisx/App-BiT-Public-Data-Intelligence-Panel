package com.appbit.geoanalytics.application.catalog.out;

import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;

import java.util.List;

public interface SourceCatalogPort {

    List<SourceSummaryDTO> findAll();
}
