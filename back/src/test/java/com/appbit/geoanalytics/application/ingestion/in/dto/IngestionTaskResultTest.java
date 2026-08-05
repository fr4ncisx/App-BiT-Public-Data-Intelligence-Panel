package com.appbit.geoanalytics.application.ingestion.in.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionTaskResultTest {

    @Test
    void ingestedResultMapsToIngestedOutcome() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", true, IngestionOutcome.INGESTED, null, 100, 98, 2);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.INGESTED);
        assertThat(result.success()).isTrue();
        assertThat(result.rowsRead()).isEqualTo(100);
        assertThat(result.rowsInserted()).isEqualTo(98);
        assertThat(result.rowsRejected()).isEqualTo(2);
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void alreadyIngestedResultMapsOutcomeWithZeroCounters() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", true, IngestionOutcome.ALREADY_INGESTED, null, 0, 0, 0);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.ALREADY_INGESTED);
        assertThat(result.success()).isTrue();
        assertThat(result.rowsRead()).isZero();
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isZero();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void skippedResultMapsToSkippedOutcome() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.SKIPPED, "File not found in storage", 0, 0, 0);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.SKIPPED);
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("File not found in storage");
    }

    @Test
    void failedResultMapsToFailedOutcome() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.FAILED, "Connection timeout", 0, 0, 0);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.FAILED);
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Connection timeout");
    }

    @Test
    void errorMessageStripsControlCharacters() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.FAILED, "Connection\ttimeout\nboom", 0, 0, 0);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.FAILED);
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Connection timeout boom");
    }

    @Test
    void errorMessageIsTruncatedToOneThousandCharacters() {
        var longMessage = "x".repeat(2000);
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.FAILED, longMessage, 0, 0, 0);

        assertThat(result.errorMessage()).hasSize(1000);
    }

    @Test
    void nullErrorMessageStaysNull() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.FAILED, null, 0, 0, 0);

        assertThat(result.outcome()).isEqualTo(IngestionOutcome.FAILED);
        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void trimsErrorMessage() {
        var result = new IngestionTaskResult(
                "antenas_flp.csv", false, IngestionOutcome.FAILED, "  spaced message  ", 0, 0, 0);

        assertThat(result.errorMessage()).isEqualTo("spaced message");
    }
}