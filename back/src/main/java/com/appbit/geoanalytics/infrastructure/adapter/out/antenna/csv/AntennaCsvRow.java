package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AntennaCsvRow(
        @JsonProperty("ecgi") String ecgi,
        @JsonProperty("cluster") String cluster,
        @JsonProperty("municipio") String municipio,
        @JsonProperty("lat") String lat,
        @JsonProperty("lon") String lon
) {
}
