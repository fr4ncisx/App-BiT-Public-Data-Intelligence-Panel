package com.appbit.geoanalytics.application.maps.out;

import java.math.BigDecimal;
import java.util.UUID;

public record FlowSummary(
        UUID originRegionId,
        UUID destinationRegionId,
        String originClusterName,
        String destinationClusterName,
        String originMunicipality,
        String destinationMunicipality,
        BigDecimal originLatitude,
        BigDecimal originLongitude,
        BigDecimal destinationLatitude,
        BigDecimal destinationLongitude,
        Boolean sameCluster,
        Long usersCount,
        Long tripsCount,
        BigDecimal averageDistanceKm,
        String predominantPeriod
) {
}
