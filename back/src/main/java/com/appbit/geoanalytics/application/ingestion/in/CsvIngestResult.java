package com.appbit.geoanalytics.application.ingestion.in;

public record CsvIngestResult(
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    public static CsvIngestResult of(int rowsRead, int rowsInserted, int rowsRejected) {
        return new CsvIngestResult(rowsRead, rowsInserted, rowsRejected);
    }
}
