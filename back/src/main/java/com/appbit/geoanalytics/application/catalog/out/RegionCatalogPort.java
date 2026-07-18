package com.appbit.geoanalytics.application.catalog.out;

import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;

import java.util.List;

public interface RegionCatalogPort {

    List<RegionSummaryDTO> findAll();
}
