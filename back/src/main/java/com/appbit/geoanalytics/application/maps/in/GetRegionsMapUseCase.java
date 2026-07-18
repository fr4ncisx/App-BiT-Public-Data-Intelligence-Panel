package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import org.jspecify.annotations.Nullable;

public interface GetRegionsMapUseCase {

    RegionsMapResponse execute(@Nullable String indicatorType, @Nullable String period, @Nullable String regionCode);
}
