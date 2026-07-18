package com.appbit.geoanalytics.application.maps.out;

import java.math.BigDecimal;
import java.util.UUID;

public record NetworkIndicatorSummary(
        UUID regionId,
        String indicatorType,
        BigDecimal score,
        String unit
) {
}
