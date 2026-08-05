package com.appbit.geoanalytics.application.ingestion.in.dto;

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

public record IngestionTaskResult(
        String fileName,
        boolean success,
        IngestionOutcome outcome,
        @Nullable String errorMessage,
        int rowsRead,
        int rowsInserted,
        int rowsRejected
) {
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cc}]");

    public IngestionTaskResult {
        if (errorMessage != null) {
            errorMessage = CONTROL_CHARACTERS.matcher(errorMessage).replaceAll(" ").trim();

            if (errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
                errorMessage = errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
            }
        }
    }
}