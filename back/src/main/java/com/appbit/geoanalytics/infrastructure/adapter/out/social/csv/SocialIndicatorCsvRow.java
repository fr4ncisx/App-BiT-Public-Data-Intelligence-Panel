package com.appbit.geoanalytics.infrastructure.adapter.out.social.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SocialIndicatorCsvRow(
        @JsonProperty("region_code") String regionCode,
        @JsonProperty("indicator_type") String indicatorType,
        @JsonProperty("score") String score,
        @JsonProperty("unit") String unit,
        @JsonProperty("gap_level") String gapLevel,
        @JsonProperty("confidence_level") String confidenceLevel,
        @JsonProperty("description") String description
) {}
