package com.appbit.geoanalytics.application.ingestion.in.dto;

public record CsvIngestResult(
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
}