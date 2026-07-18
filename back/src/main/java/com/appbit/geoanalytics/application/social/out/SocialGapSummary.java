package com.appbit.geoanalytics.application.social.out;

import java.math.BigDecimal;
import java.util.UUID;

public record SocialGapSummary(
        UUID regionId,
        String regionCode,
        String regionName,
        String municipality,
        BigDecimal score,
        String unit,
        String gapLevel,
        String confidenceLevel,
        String description,
        String sourceType
) {
}
