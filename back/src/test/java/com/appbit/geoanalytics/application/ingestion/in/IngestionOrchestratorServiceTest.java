package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionOutcome;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestionOrchestratorServiceTest {

    @Mock
    private DatasetObjectStoragePort storagePort;

    @Mock
    private DataSourcePort dataSourcePort;

    @Mock
    private IngestionRunPort ingestionRunPort;

    @Mock
    private CsvIngestService antennaService;

    @Mock
    private CsvIngestService concentrationService;

    @Mock
    private CsvIngestService socialService;

    private IngestionOrchestratorService orchestrator;

    private final Executor directExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        when(antennaService.supportedFileName()).thenReturn("antenas_flp.csv");
        when(concentrationService.supportedFileName()).thenReturn("tensor_concentracao.csv");
        when(socialService.supportedFileName()).thenReturn("social_indicators_seed.csv");
        orchestrator = orchestratorWithSkipped(Set.of());
    }

    private IngestionOrchestratorService orchestratorWithSkipped(Set<String> skippedFileNames) {
        return new IngestionOrchestratorService(
                storagePort, dataSourcePort, ingestionRunPort,
                List.of(antennaService, concentrationService, socialService),
                new RetryPolicy(3, 0), skippedFileNames, directExecutor);
    }

    @Test
    void shouldIngestAllFilesSuccessfully() {
        var antenasEntry = entry("antenas_flp.csv");
        var concentrationEntry = entry("tensor_concentracao.csv");
        var socialEntry = entry("social_indicators_seed.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(antenasEntry, concentrationEntry, socialEntry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenReturn(new CsvIngestResult(100, 98, 2));
        when(concentrationService.ingest(any())).thenReturn(new CsvIngestResult(200, 195, 5));
        when(socialService.ingest(any())).thenReturn(new CsvIngestResult(50, 50, 0));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(IngestionTaskResult::success);
        assertThat(results.get(0).fileName()).isEqualTo("antenas_flp.csv");
        assertThat(results.get(0).rowsRead()).isEqualTo(100);
        assertThat(results.get(1).fileName()).isEqualTo("tensor_concentracao.csv");
        assertThat(results.get(1).rowsInserted()).isEqualTo(195);
        assertThat(results.get(2).fileName()).isEqualTo("social_indicators_seed.csv");
        assertThat(results.get(2).rowsRejected()).isZero();
        assertThat(results).extracting(IngestionTaskResult::outcome)
                .containsOnly(IngestionOutcome.INGESTED);
        assertThat(results.get(0).outcome()).isEqualTo(IngestionOutcome.INGESTED);
    }

    @Test
    void shouldSkipFileWhenNotExistsInStorage() {
        var entry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(false);

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().outcome()).isEqualTo(IngestionOutcome.SKIPPED);
        assertThat(results.getFirst().success()).isFalse();
        assertThat(results.getFirst().errorMessage()).contains("File not found");
    }

    @Test
    void shouldFailWhenRetriesAreExhausted() {
        var entry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenThrow(new RuntimeException("Connection timeout"));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().outcome()).isEqualTo(IngestionOutcome.FAILED);
        assertThat(results.getFirst().success()).isFalse();
        assertThat(results.getFirst().errorMessage()).contains("Connection timeout");
        verify(antennaService, times(3)).ingest(any());
    }

    @Test
    void shouldRetryAfterTransientFailure() {
        var entry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any()))
                .thenThrow(new RuntimeException("Connection timeout"))
                .thenReturn(new CsvIngestResult(100, 98, 2));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().outcome()).isEqualTo(IngestionOutcome.INGESTED);
        assertThat(results.getFirst().success()).isTrue();
        assertThat(results.getFirst().rowsRead()).isEqualTo(100);
        verify(antennaService, times(2)).ingest(any());
    }

    @Test
    void shouldFailFastOnDuplicateSupportedFileNames() {
        var duplicate = mock(CsvIngestService.class);
        when(duplicate.supportedFileName()).thenReturn("antenas_flp.csv");
        var brokenOrchestrator = new IngestionOrchestratorService(
                storagePort, dataSourcePort, ingestionRunPort,
                List.of(antennaService, duplicate),
                new RetryPolicy(3, 0), Set.of(), directExecutor);

        assertThatThrownBy(brokenOrchestrator::executeAll)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("antenas_flp.csv");
    }

    @Test
    void shouldSkipFilesWithoutRegisteredService() {
        var unknownEntry = entry("unknown_file.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(unknownEntry));

        var results = orchestrator.executeAll().join();

        assertThat(results).isEmpty();
    }

    @Test
    void shouldSkipSilentlyFilesWithoutServiceInSkippedSet() {
        var orchestrator = orchestratorWithSkipped(Set.of("tensor_mobilidade.csv"));
        var unknownEntry = entry("tensor_mobilidade.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(unknownEntry));

        var results = orchestrator.executeAll().join();

        assertThat(results).isEmpty();
    }

    @Test
    void shouldStillIngestFileWhenInSkippedSetButServiceRegistered() {
        var orchestrator = orchestratorWithSkipped(Set.of("antenas_flp.csv"));
        var knownEntry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(knownEntry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenReturn(new CsvIngestResult(10, 10, 0));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().fileName()).isEqualTo("antenas_flp.csv");
        assertThat(results.getFirst().outcome()).isEqualTo(IngestionOutcome.INGESTED);
    }

    @Test
    void shouldIngestOnlyMatchingFilesFromDataSource() {
        var knownEntry = entry("antenas_flp.csv");
        var unknownEntry = entry("tensor_mobilidade.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(knownEntry, unknownEntry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenReturn(new CsvIngestResult(10, 10, 0));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().fileName()).isEqualTo("antenas_flp.csv");
    }

    @Test
    void shouldMarkAlreadyIngestedFilesWithoutStorageCall() {
        var entry = entry("antenas_flp.csv");
        var completedRun = mock(IngestionRun.class);
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(entry.id(), "antenas_flp.csv"))
                .thenReturn(Optional.of(completedRun));
        when(completedRun.getState()).thenReturn(IngestionState.COMPLETED);

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().outcome()).isEqualTo(IngestionOutcome.ALREADY_INGESTED);
        assertThat(results.getFirst().success()).isTrue();
        assertThat(results.getFirst().rowsRead()).isZero();
        assertThat(results.getFirst().rowsInserted()).isZero();
        assertThat(results.getFirst().rowsRejected()).isZero();
        verify(storagePort, never()).exists(any());
    }

    private static SourceCatalogEntry entry(String fileName) {
        return new SourceCatalogEntry(UUID.randomUUID(), "Test", new SourceFileName(fileName),
                DataSourceType.SYNTHETIC_DATASET, "Test description", null, null, null, null);
    }
}