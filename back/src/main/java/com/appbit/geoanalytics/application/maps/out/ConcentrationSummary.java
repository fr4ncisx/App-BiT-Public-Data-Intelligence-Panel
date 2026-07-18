package com.appbit.geoanalytics.application.maps.out;

import java.math.BigDecimal;
import java.util.UUID;

public record ConcentrationSummary(
        UUID regionId,
        Long totalActiveUsers,
        BigDecimal avgCongestion
) {
}
