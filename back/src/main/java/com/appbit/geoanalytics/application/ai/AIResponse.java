package com.appbit.geoanalytics.application.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AIResponse(
        @JsonProperty(required = true, value = "summary") String summary,

        @JsonProperty(required = true, value = "explanation") String explanation,

        @JsonProperty(required = true, value = "suggestedVisualization") String suggestedVisualization
) {
}
