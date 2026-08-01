package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.domain.testing.DomainFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestionLifecycleManagerTest {

    @Mock private IngestionRunPort ingestionRunPort;
    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private TransactionTemplate transactionTemplate;

    private IngestionLifecycleManager service;

    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private static final UUID RUN_ID = DomainFixtures.uuidV7();
    private static final String FILE_NAME = "tensor_fluxo_vias.csv";

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(RUN_ID);
        when(ingestionRunPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        service = new IngestionLifecycleManager(ingestionRunPort, idGeneratorPort, transactionTemplate);
    }

    private IngestionRun createRun(IngestionState state, Instant finishedAt) {
        return IngestionRun.builder()
                .id(new IngestionRunId(RUN_ID))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName(FILE_NAME))
                .state(state)
                .startedAt(Instant.now())
                .finishedAt(finishedAt)
                .errorMessage(state == IngestionState.FAILED ? "Previous failure" : null)
                .build();
    }

    @Test
    void startCreatesNewRunWhenNoPreviousRunExists() {
        when(ingestionRunPort.findLatestBySourceIdAndFileName(SOURCE_ID, FILE_NAME)).thenReturn(Optional.empty());

        IngestionRun result = service.start(FILE_NAME, SOURCE_ID);

        assertThat(result.getState()).isEqualTo(IngestionState.RUNNING);
        verify(idGeneratorPort).generate();
        verify(ingestionRunPort).save(result);
    }

    @Test
    void startReturnsNullWhenLastRunCompleted() {
        var completed = createRun(IngestionState.COMPLETED, Instant.now());
        when(ingestionRunPort.findLatestBySourceIdAndFileName(SOURCE_ID, FILE_NAME))
                .thenReturn(Optional.of(completed));

        IngestionRun result = service.start(FILE_NAME, SOURCE_ID);

        assertThat(result).isNull();
        verify(ingestionRunPort, never()).save(any());
    }

    @Test
    void startFailsAndResetsRunLeftInRunningState() {
        var running = createRun(IngestionState.RUNNING, null);
        when(ingestionRunPort.findLatestBySourceIdAndFileName(SOURCE_ID, FILE_NAME))
                .thenReturn(Optional.of(running));

        IngestionRun result = service.start(FILE_NAME, SOURCE_ID);

        assertThat(result).isSameAs(running);
        assertThat(result.getState()).isEqualTo(IngestionState.RUNNING);
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getRowsRead()).isZero();
        verify(ingestionRunPort).save(running);
    }

    @Test
    void startResetsFailedRunAndStartsAgain() {
        var failed = createRun(IngestionState.FAILED, Instant.now());
        when(ingestionRunPort.findLatestBySourceIdAndFileName(SOURCE_ID, FILE_NAME))
                .thenReturn(Optional.of(failed));

        IngestionRun result = service.start(FILE_NAME, SOURCE_ID);

        assertThat(result).isSameAs(failed);
        assertThat(result.getState()).isEqualTo(IngestionState.RUNNING);
        assertThat(result.getFinishedAt()).isNull();
        verify(ingestionRunPort).save(failed);
    }

    @Test
    void completeRegistersCountersAndCompletes() {
        var run = createRun(IngestionState.RUNNING, null);

        service.complete(run, 5, 3, 1);

        assertThat(run.getRowsRead()).isEqualTo(5);
        assertThat(run.getRowsInserted()).isEqualTo(3);
        assertThat(run.getRowsRejected()).isEqualTo(1);
        assertThat(run.getState()).isEqualTo(IngestionState.COMPLETED);
        verify(ingestionRunPort).save(run);
    }

    @Test
    void completeWithZeroCountersCompletesWithoutRegistrations() {
        var run = createRun(IngestionState.RUNNING, null);

        service.complete(run, 0, 0, 0);

        assertThat(run.getRowsRead()).isZero();
        assertThat(run.getState()).isEqualTo(IngestionState.COMPLETED);
        verify(ingestionRunPort).save(run);
    }

    @Test
    void failPersistsFailedStateWithErrorMessage() {
        var run = createRun(IngestionState.RUNNING, null);

        service.fail(run, new RuntimeException("Storage unavailable"));

        assertThat(run.getState()).isEqualTo(IngestionState.FAILED);
        assertThat(run.getErrorMessage()).isEqualTo("Storage unavailable");
        verify(transactionTemplate).executeWithoutResult(any());
        verify(ingestionRunPort).save(run);
    }

    @Test
    void failTruncatesLongErrorMessage() {
        var run = createRun(IngestionState.RUNNING, null);
        var longMessage = "x".repeat(1000);

        service.fail(run, new RuntimeException(longMessage));

        assertThat(run.getState()).isEqualTo(IngestionState.FAILED);
        assertThat(run.getErrorMessage()).hasSize(953).endsWith("...");
    }

    @Test
    void failUsesClassNameWhenMessageIsNull() {
        var run = createRun(IngestionState.RUNNING, null);

        service.fail(run, new RuntimeException());

        assertThat(run.getErrorMessage()).isEqualTo("RuntimeException");
        assertThat(run.getState()).isEqualTo(IngestionState.FAILED);
    }

    @Test
    void failSwallowsPersistenceErrors() {
        var run = createRun(IngestionState.RUNNING, null);
        org.mockito.Mockito.doThrow(new RuntimeException("Transaction failed"))
                .when(transactionTemplate).executeWithoutResult(any());

        service.fail(run, new RuntimeException("boom"));

        assertThat(run.getState()).isEqualTo(IngestionState.RUNNING);
        verify(ingestionRunPort, never()).save(any());
    }

    @Test
    void failPersistsErrorMessageWithoutControlCharacters() {
        var run = createRun(IngestionState.RUNNING, null);

        service.fail(run, new RuntimeException("line1\nline2"));

        assertThat(run.getErrorMessage()).doesNotContain("\n");
    }

    @Test
    void saveCapturesRunWithGeneratedId() {
        when(ingestionRunPort.findLatestBySourceIdAndFileName(SOURCE_ID, FILE_NAME)).thenReturn(Optional.empty());

        service.start(FILE_NAME, SOURCE_ID);

        var captor = ArgumentCaptor.forClass(IngestionRun.class);
        verify(ingestionRunPort).save(captor.capture());
        assertThat(captor.getValue().getId().value()).isEqualTo(RUN_ID);
    }
}
