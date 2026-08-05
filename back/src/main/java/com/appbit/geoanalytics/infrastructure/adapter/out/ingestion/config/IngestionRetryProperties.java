package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "appbit.ingestion.retry")
public record IngestionRetryProperties(
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("2000") long baseDelayMs
) {
}