package com.appbit.geoanalytics.application.antenna.in;

public record AntennaIngestResult(
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    public static AntennaIngestResult of(int rowsRead, int rowsInserted, int rowsRejected) {
        return new AntennaIngestResult(rowsRead, rowsInserted, rowsRejected);
    }
}
