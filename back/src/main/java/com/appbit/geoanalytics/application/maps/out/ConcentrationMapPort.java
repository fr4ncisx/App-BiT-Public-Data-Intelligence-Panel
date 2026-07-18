package com.appbit.geoanalytics.application.maps.out;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ConcentrationMapPort {

    List<ConcentrationSummary> findByRegionIds(List<UUID> regionIds, @Nullable String period);
}
