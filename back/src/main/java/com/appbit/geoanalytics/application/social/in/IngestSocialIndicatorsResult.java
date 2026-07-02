package com.appbit.geoanalytics.application.social.in;

public record IngestSocialIndicatorsResult(
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    public static IngestSocialIndicatorsResult of(int rowsRead, int rowsInserted, int rowsRejected) {
        return new IngestSocialIndicatorsResult(rowsRead, rowsInserted, rowsRejected);
    }
}
