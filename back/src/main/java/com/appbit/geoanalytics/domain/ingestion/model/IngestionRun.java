package com.appbit.geoanalytics.domain.ingestion.model;

import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.exception.IngestionDomainException;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

@Getter
public final class IngestionRun {

    private final IngestionRunId id;
    private final DataSourceId sourceId;
    private final SourceFileName fileName;

    private Instant startedAt;
    private IngestionState state;
    private long rowsRead;
    private long rowsInserted;
    private long rowsRejected;

    @Nullable
    private Instant finishedAt;

    private String errorMessage;

    @Builder
    public IngestionRun(
            IngestionRunId id,
            DataSourceId sourceId,
            SourceFileName fileName,
            IngestionState state,
            long rowsRead,
            long rowsInserted,
            long rowsRejected,
            Instant startedAt,
            @Nullable Instant finishedAt,
            @Nullable String errorMessage
    ) {
        if (id == null) {
            throw new IngestionDomainException("Ingestion run id cannot be null");
        }

        if (sourceId == null) {
            throw new IngestionDomainException("Data source id cannot be null");
        }

        if (fileName == null) {
            throw new IngestionDomainException("Source file name cannot be null");
        }

        if (state == null) {
            throw new IngestionDomainException("Ingestion state cannot be null");
        }

        if (startedAt == null) {
            throw new IngestionDomainException("Started at cannot be null");
        }

        validateCounters(rowsRead, rowsInserted, rowsRejected);
        validateFinishedAt(startedAt, finishedAt, state);
        validateErrorMessage(errorMessage, state);

        this.id = id;
        this.sourceId = sourceId;
        this.fileName = fileName;
        this.state = state;
        this.rowsRead = rowsRead;
        this.rowsInserted = rowsInserted;
        this.rowsRejected = rowsRejected;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.errorMessage = normalizeErrorMessage(errorMessage);
    }

    public void registerReadRows(long amount) {
        validatePositiveAmount(amount, "Read rows amount");

        if (state.isFinished()) {
            throw new IngestionDomainException("Cannot register read rows on finished ingestion run");
        }

        rowsRead += amount;
    }

    public void registerInsertedRows(long amount) {
        validatePositiveAmount(amount, "Inserted rows amount");

        if (state.isFinished()) {
            throw new IngestionDomainException("Cannot register inserted rows on finished ingestion run");
        }

        if (rowsInserted + rowsRejected + amount > rowsRead) {
            throw new IngestionDomainException("Inserted and rejected rows cannot exceed read rows");
        }

        rowsInserted += amount;
    }

    public void registerRejectedRows(long amount) {
        validatePositiveAmount(amount, "Rejected rows amount");

        if (state.isFinished()) {
            throw new IngestionDomainException("Cannot register rejected rows on finished ingestion run");
        }

        if (rowsInserted + rowsRejected + amount > rowsRead) {
            throw new IngestionDomainException("Inserted and rejected rows cannot exceed read rows");
        }

        rowsRejected += amount;
    }

    public void complete(Instant finishedAt) {
        if (state.isFinished()) {
            throw new IngestionDomainException("Ingestion run is already finished");
        }

        if (finishedAt == null) {
            throw new IngestionDomainException("Finished at cannot be null");
        }

        validateFinishedAt(this.startedAt, finishedAt, IngestionState.COMPLETED);

        this.state = IngestionState.COMPLETED;
        this.finishedAt = finishedAt;
        this.errorMessage = "";
    }

    public void fail(Instant finishedAt, String errorMessage) {
        if (state.isFinished()) {
            throw new IngestionDomainException("Ingestion run is already finished");
        }

        if (finishedAt == null) {
            throw new IngestionDomainException("Finished at cannot be null");
        }

        validateFinishedAt(this.startedAt, finishedAt, IngestionState.FAILED);
        validateErrorMessage(errorMessage, IngestionState.FAILED);

        this.state = IngestionState.FAILED;
        this.finishedAt = finishedAt;
        this.errorMessage = normalizeErrorMessage(errorMessage);
    }

    public void reset(Instant newStartedAt) {
        if (!state.isFinished()) {
            throw new IngestionDomainException("Can only reset a finished ingestion run");
        }

        if (newStartedAt == null) {
            throw new IngestionDomainException("New started at cannot be null");
        }

        this.state = IngestionState.RUNNING;
        this.startedAt = newStartedAt;
        this.rowsRead = 0;
        this.rowsInserted = 0;
        this.rowsRejected = 0;
        this.finishedAt = null;
        this.errorMessage = null;
    }

    private void validateCounters(long rowsRead, long rowsInserted, long rowsRejected) {
        if (rowsRead < 0) {
            throw new IngestionDomainException("Rows read cannot be negative");
        }

        if (rowsInserted < 0) {
            throw new IngestionDomainException("Rows inserted cannot be negative");
        }

        if (rowsRejected < 0) {
            throw new IngestionDomainException("Rows rejected cannot be negative");
        }

        if (rowsInserted + rowsRejected > rowsRead) {
            throw new IngestionDomainException("Inserted and rejected rows cannot exceed read rows");
        }
    }

    private void validateFinishedAt(
            Instant startedAt,
            @Nullable Instant finishedAt,
            IngestionState state
    ) {
        if (state.isFinished() && finishedAt == null) {
            throw new IngestionDomainException("Finished at is mandatory for finished ingestion run");
        }

        if (!state.isFinished() && finishedAt != null) {
            throw new IngestionDomainException("Finished at must be null for unfinished ingestion run");
        }

        if (finishedAt != null && finishedAt.isBefore(startedAt)) {
            throw new IngestionDomainException("Finished at cannot be before started at");
        }
    }

    private void validateErrorMessage(@Nullable String errorMessage, IngestionState state) {
        String normalized = normalizeErrorMessage(errorMessage);

        if (state.requiresErrorMessage() && normalized.isBlank()) {
            throw new IngestionDomainException("Error message is mandatory for " + state.name());
        }

        if (!normalized.isBlank() && normalized.length() > 1000) {
            throw new IngestionDomainException("Error message cannot exceed 1000 characters");
        }
    }

    private void validatePositiveAmount(long amount, String fieldName) {
        if (amount <= 0) {
            throw new IngestionDomainException(fieldName + " must be greater than zero");
        }
    }

    private String normalizeErrorMessage(@Nullable String errorMessage) {
        if (errorMessage == null) return "";
        return errorMessage.replaceAll("[\\p{Cc}]", " ").trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IngestionRun that = (IngestionRun) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
