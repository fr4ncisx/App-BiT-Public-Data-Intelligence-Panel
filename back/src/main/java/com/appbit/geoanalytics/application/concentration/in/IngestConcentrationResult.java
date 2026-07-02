package com.appbit.geoanalytics.application.concentration.in;

public record IngestConcentrationResult(
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    public static IngestConcentrationResult of(int rowsRead, int rowsInserted, int rowsRejected) {
        return new IngestConcentrationResult(rowsRead, rowsInserted, rowsRejected);
    }
}
