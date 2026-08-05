package com.appbit.geoanalytics.application.ingestion.in.dto;

public enum IngestionOutcome {
    INGESTED,
    ALREADY_INGESTED,
    SKIPPED,
    FAILED
}