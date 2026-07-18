package com.appbit.geoanalytics.domain.ingestion.model;

import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.exception.IngestionDomainException;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.appbit.geoanalytics.domain.testing.DomainFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionRunTest {

    private static final Instant STARTED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant FINISHED_AT = Instant.parse("2026-01-15T10:05:00Z");

    @Test
    void shouldCreateRunningRunAndNormalizeBlankErrorMessage() {
        IngestionRun run = new IngestionRun(
                ingestionRunId(),
                dataSourceId(),
                sourceFileName(),
                IngestionState.RUNNING,
                10L,
                5L,
                2L,
                STARTED_AT,
                null,
                "   "
        );

        assertThat(run.getState()).isEqualTo(IngestionState.RUNNING);
        assertThat(run.getRowsRead()).isEqualTo(10L);
        assertThat(run.getErrorMessage()).isEmpty();
        assertThat(run.getFinishedAt()).isNull();
    }

    @Test
    void shouldCreateFinishedRunWhenFinishedAtIsPresent() {
        IngestionRun run = new IngestionRun(
                ingestionRunId(),
                dataSourceId(),
                sourceFileName(),
                IngestionState.COMPLETED,
                10L,
                8L,
                2L,
                STARTED_AT,
                FINISHED_AT,
                null
        );

        assertThat(run.getState()).isEqualTo(IngestionState.COMPLETED);
        assertThat(run.getFinishedAt()).isEqualTo(FINISHED_AT);
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThatThrownBy(() -> runWith(null, dataSourceId(), sourceFileName(), IngestionState.RUNNING, STARTED_AT, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run id cannot be null");
        assertThatThrownBy(() -> runWith(ingestionRunId(), null, sourceFileName(), IngestionState.RUNNING, STARTED_AT, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Data source id cannot be null");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), null, IngestionState.RUNNING, STARTED_AT, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Source file name cannot be null");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), null, STARTED_AT, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion state cannot be null");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.RUNNING, null, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Started at cannot be null");
    }

    @Test
    void shouldValidateCountersOnCreation() {
        assertThatThrownBy(() -> runWithCounters(-1L, 0L, 0L))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Rows read cannot be negative");
        assertThatThrownBy(() -> runWithCounters(1L, -1L, 0L))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Rows inserted cannot be negative");
        assertThatThrownBy(() -> runWithCounters(1L, 0L, -1L))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Rows rejected cannot be negative");
        assertThatThrownBy(() -> runWithCounters(1L, 1L, 1L))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Inserted and rejected rows cannot exceed read rows");
    }

    @Test
    void shouldValidateFinishedAtAgainstState() {
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.COMPLETED, STARTED_AT, null, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Finished at is mandatory for finished ingestion run");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.RUNNING, STARTED_AT, FINISHED_AT, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Finished at must be null for unfinished ingestion run");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.COMPLETED, STARTED_AT, STARTED_AT.minusSeconds(1), null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Finished at cannot be before started at");
    }

    @Test
    void shouldValidateErrorMessageAgainstState() {
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.FAILED, STARTED_AT, FINISHED_AT, null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Error message is mandatory for FAILED");
        assertThatThrownBy(() -> runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.FAILED, STARTED_AT, FINISHED_AT, text(1001)))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Error message cannot exceed 1000 characters");
        assertThat(runWith(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.FAILED, STARTED_AT, FINISHED_AT, "fallo\ncritico").getErrorMessage())
                .isEqualTo("fallo critico");
    }

    @Test
    void shouldRegisterCountersWhileRunIsUnfinished() {
        IngestionRun run = new IngestionRun(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.RUNNING, 0L, 0L, 0L, STARTED_AT, null, null);

        run.registerReadRows(10L);
        run.registerInsertedRows(7L);
        run.registerRejectedRows(3L);

        assertThat(run.getRowsRead()).isEqualTo(10L);
        assertThat(run.getRowsInserted()).isEqualTo(7L);
        assertThat(run.getRowsRejected()).isEqualTo(3L);
    }

    @Test
    void shouldRejectInvalidCounterMutations() {
        IngestionRun run = new IngestionRun(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.RUNNING, 5L, 2L, 1L, STARTED_AT, null, null);

        assertThatThrownBy(() -> run.registerReadRows(0L)).isInstanceOf(IngestionDomainException.class).hasMessage("Read rows amount must be greater than zero");
        assertThatThrownBy(() -> run.registerInsertedRows(0L)).isInstanceOf(IngestionDomainException.class).hasMessage("Inserted rows amount must be greater than zero");
        assertThatThrownBy(() -> run.registerRejectedRows(0L)).isInstanceOf(IngestionDomainException.class).hasMessage("Rejected rows amount must be greater than zero");
        assertThatThrownBy(() -> run.registerInsertedRows(3L)).isInstanceOf(IngestionDomainException.class).hasMessage("Inserted and rejected rows cannot exceed read rows");
        assertThatThrownBy(() -> run.registerRejectedRows(3L)).isInstanceOf(IngestionDomainException.class).hasMessage("Inserted and rejected rows cannot exceed read rows");
    }

    @Test
    void shouldCompleteRun() {
        IngestionRun run = ingestionRun();

        run.complete(FINISHED_AT);

        assertThat(run.getState()).isEqualTo(IngestionState.COMPLETED);
        assertThat(run.getFinishedAt()).isEqualTo(FINISHED_AT);
        assertThat(run.getErrorMessage()).isEmpty();
    }

    @Test
    void shouldFailRun() {
        IngestionRun run = ingestionRun();

        run.fail(FINISHED_AT, "  error de parseo  ");

        assertThat(run.getState()).isEqualTo(IngestionState.FAILED);
        assertThat(run.getFinishedAt()).isEqualTo(FINISHED_AT);
        assertThat(run.getErrorMessage()).isEqualTo("error de parseo");
    }

    @Test
    void shouldRejectInvalidTerminalTransitions() {
        IngestionRun run = ingestionRun();

        assertThatThrownBy(() -> run.complete(null)).isInstanceOf(IngestionDomainException.class).hasMessage("Finished at cannot be null");
        assertThatThrownBy(() -> run.fail(null, "error valido")).isInstanceOf(IngestionDomainException.class).hasMessage("Finished at cannot be null");
        assertThatThrownBy(() -> run.complete(STARTED_AT.minusSeconds(1))).isInstanceOf(IngestionDomainException.class).hasMessage("Finished at cannot be before started at");
        assertThatThrownBy(() -> run.fail(FINISHED_AT, "   ")).isInstanceOf(IngestionDomainException.class).hasMessage("Error message is mandatory for FAILED");
    }

    @Test
    void shouldRejectMutationsWhenRunIsFinished() {
        IngestionRun run = ingestionRun();
        run.complete(FINISHED_AT);

        assertThatThrownBy(() -> run.registerReadRows(1L)).isInstanceOf(IngestionDomainException.class).hasMessage("Cannot register read rows on finished ingestion run");
        assertThatThrownBy(() -> run.registerInsertedRows(1L)).isInstanceOf(IngestionDomainException.class).hasMessage("Cannot register inserted rows on finished ingestion run");
        assertThatThrownBy(() -> run.registerRejectedRows(1L)).isInstanceOf(IngestionDomainException.class).hasMessage("Cannot register rejected rows on finished ingestion run");
        assertThatThrownBy(() -> run.complete(FINISHED_AT.plusSeconds(1))).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run is already finished");
        assertThatThrownBy(() -> run.fail(FINISHED_AT.plusSeconds(1), "error valido")).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run is already finished");
    }

    @Test
    void shouldResetFinishedRunToRunning() {
        IngestionRun run = ingestionRun();
        run.registerReadRows(10L);
        run.registerInsertedRows(8L);
        run.registerRejectedRows(2L);
        run.complete(FINISHED_AT);

        var newStartedAt = Instant.parse("2026-01-16T08:00:00Z");
        run.reset(newStartedAt);

        assertThat(run.getState()).isEqualTo(IngestionState.RUNNING);
        assertThat(run.getStartedAt()).isEqualTo(newStartedAt);
        assertThat(run.getRowsRead()).isEqualTo(0L);
        assertThat(run.getRowsInserted()).isEqualTo(0L);
        assertThat(run.getRowsRejected()).isEqualTo(0L);
        assertThat(run.getFinishedAt()).isNull();
        assertThat(run.getErrorMessage()).isNull();
    }

    @Test
    void shouldRejectResetOnUnfinishedRun() {
        IngestionRun run = ingestionRun();

        assertThatThrownBy(() -> run.reset(Instant.now()))
                .isInstanceOf(IngestionDomainException.class).hasMessage("Can only reset a finished ingestion run");
    }

    @Test
    void shouldRejectResetWithNullStartedAt() {
        IngestionRun run = ingestionRun();
        run.complete(FINISHED_AT);

        assertThatThrownBy(() -> run.reset(null))
                .isInstanceOf(IngestionDomainException.class).hasMessage("New started at cannot be null");
    }

    @Test
    void shouldCompareRunsByIdentity() {
        IngestionRunId sameId = ingestionRunId();
        IngestionRun first = runWith(sameId, dataSourceId(), sourceFileName(), IngestionState.RUNNING, STARTED_AT, null, null);
        IngestionRun second = runWith(sameId, dataSourceId(), sourceFileName(), IngestionState.RUNNING, STARTED_AT.plusSeconds(1), null, null);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(ingestionRun());
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("not a run");
    }

    private IngestionRun runWith(
            IngestionRunId id,
            com.appbit.geoanalytics.domain.source.vo.DataSourceId sourceId,
            com.appbit.geoanalytics.domain.source.vo.SourceFileName fileName,
            IngestionState state,
            Instant startedAt,
            Instant finishedAt,
            String errorMessage
    ) {
        return new IngestionRun(id, sourceId, fileName, state, 10L, 5L, 2L, startedAt, finishedAt, errorMessage);
    }

    private IngestionRun runWithCounters(long rowsRead, long rowsInserted, long rowsRejected) {
        return new IngestionRun(ingestionRunId(), dataSourceId(), sourceFileName(), IngestionState.RUNNING, rowsRead, rowsInserted, rowsRejected, STARTED_AT, null, null);
    }

    @Nested
    class IngestionRunIdTest {

        @Test
        void shouldAcceptUuidV7() {
            IngestionRunId id = new IngestionRunId(uuidV7());

            assertThat(id.value().version()).isEqualTo(7);
            assertThat(id.value().variant()).isEqualTo(2);
        }

        @Test
        void shouldRejectInvalidUuid() {
            assertThatThrownBy(() -> new IngestionRunId(null)).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run id cannot be null");
            assertThatThrownBy(() -> new IngestionRunId(nilUuid())).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run id cannot be nil UUID");
            assertThatThrownBy(() -> new IngestionRunId(uuidV4())).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run id must be UUIDv7");
            assertThatThrownBy(() -> new IngestionRunId(nonRfc4122UuidV7())).isInstanceOf(IngestionDomainException.class).hasMessage("Ingestion run id must be RFC 4122 compatible");
        }
    }
}
