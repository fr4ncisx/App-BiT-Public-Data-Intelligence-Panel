package com.appbit.geoanalytics.application.ingestion.in;

public record RetryPolicy(int maxAttempts, long baseDelayMillis) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be greater than or equal to 1");
        }

        if (baseDelayMillis < 0) {
            throw new IllegalArgumentException("baseDelayMillis must be greater than or equal to 0");
        }
    }
}