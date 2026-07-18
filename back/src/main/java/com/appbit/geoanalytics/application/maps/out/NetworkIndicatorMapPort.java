package com.appbit.geoanalytics.application.maps.out;

import java.util.List;
import java.util.UUID;

public interface NetworkIndicatorMapPort {

    List<NetworkIndicatorSummary> findByRegionIds(List<UUID> regionIds);
}
