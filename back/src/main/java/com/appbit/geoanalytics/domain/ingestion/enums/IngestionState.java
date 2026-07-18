package com.appbit.geoanalytics.domain.ingestion.enums;

public enum IngestionState {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED;

    public boolean isFinished() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean requiresErrorMessage() {
        return this == FAILED;
    }
}