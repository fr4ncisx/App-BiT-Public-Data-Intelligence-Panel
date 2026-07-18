package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.maps.CompareRegionsResponse;

public interface CompareRegionsUseCase {

    CompareRegionsResponse execute(String regionCodeA, String regionCodeB);
}
