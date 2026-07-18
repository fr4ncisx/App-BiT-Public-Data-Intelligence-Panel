package com.appbit.geoanalytics.application.ingestion.in.dto;

import org.jspecify.annotations.Nullable;

public record IngestionTaskResult(
        String fileName,
        boolean success,
        @Nullable String errorMessage,
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    public static IngestionTaskResult success(String fileName, CsvIngestResult result) {
        return new IngestionTaskResult(fileName, true, null,
                result.rowsRead(), result.rowsInserted(), result.rowsRejected());
    }

    public static IngestionTaskResult skipped(String fileName, String errorMessage) {
        return new IngestionTaskResult(fileName, false, errorMessage, 0, 0, 0);
    }

    public static IngestionTaskResult failed(String fileName, String errorMessage) {
        return new IngestionTaskResult(fileName, false, errorMessage, 0, 0, 0);
    }
}
