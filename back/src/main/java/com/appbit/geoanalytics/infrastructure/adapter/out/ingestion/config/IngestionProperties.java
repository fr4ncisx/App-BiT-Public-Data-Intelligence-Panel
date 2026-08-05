package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "appbit.ingestion")
public record IngestionProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("500") int batchSize,
        @DefaultValue("true") boolean failFast
) {
    public IngestionProperties {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be greater than or equal to 1");
        }
    }
}