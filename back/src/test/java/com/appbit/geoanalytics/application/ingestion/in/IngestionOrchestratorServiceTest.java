package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        when(antennaService.supportedFileName()).thenReturn("antenas_flp.csv");
        when(concentrationService.supportedFileName()).thenReturn("tensor_concentracao.csv");
        when(socialService.supportedFileName()).thenReturn("social_indicators_seed.csv");
        orchestrator = new IngestionOrchestratorService(
                storagePort, dataSourcePort, ingestionRunPort,
                List.of(antennaService, concentrationService, socialService));
    }

    @Test
    void shouldIngestAllFilesSuccessfully() {
        var antenasEntry = entry("antenas_flp.csv");
        var concentrationEntry = entry("tensor_concentracao.csv");
        var socialEntry = entry("social_indicators_seed.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(antenasEntry, concentrationEntry, socialEntry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenReturn(CsvIngestResult.of(100, 98, 2));
        when(concentrationService.ingest(any())).thenReturn(CsvIngestResult.of(200, 195, 5));
        when(socialService.ingest(any())).thenReturn(CsvIngestResult.of(50, 50, 0));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(IngestionTaskResult::success);
        assertThat(results.get(0).fileName()).isEqualTo("antenas_flp.csv");
        assertThat(results.get(0).rowsRead()).isEqualTo(100);
        assertThat(results.get(1).fileName()).isEqualTo("tensor_concentracao.csv");
        assertThat(results.get(1).rowsInserted()).isEqualTo(195);
        assertThat(results.get(2).fileName()).isEqualTo("social_indicators_seed.csv");
        assertThat(results.get(2).rowsRejected()).isEqualTo(0);
    }

    @Test
    void shouldSkipFileWhenNotExistsInStorage() {
        var entry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(false);

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().success()).isFalse();
        assertThat(results.getFirst().errorMessage()).contains("File not found");
    }

    @Test
    void shouldHandleIngestionException() {
        var entry = entry("antenas_flp.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenThrow(new RuntimeException("Connection timeout"));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().success()).isFalse();
        assertThat(results.getFirst().errorMessage()).contains("Connection timeout");
    }

    @Test
    void shouldSkipFilesWithoutRegisteredService() {
        var unknownEntry = entry("unknown_file.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(unknownEntry));

        var results = orchestrator.executeAll().join();

        assertThat(results).isEmpty();
    }

    @Test
    void shouldIngestOnlyMatchingFilesFromDataSource() {
        var knownEntry = entry("antenas_flp.csv");
        var unknownEntry = entry("tensor_mobilidade.csv");
        when(dataSourcePort.findAll()).thenReturn(List.of(knownEntry, unknownEntry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(any(), any())).thenReturn(Optional.empty());
        when(storagePort.exists(any())).thenReturn(true);
        when(antennaService.ingest(any())).thenReturn(CsvIngestResult.of(10, 10, 0));

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().fileName()).isEqualTo("antenas_flp.csv");
    }

    @Test
    void shouldSkipAlreadyIngestedFilesWithoutStorageCall() {
        var entry = entry("antenas_flp.csv");
        var completedRun = mock(IngestionRun.class);
        when(dataSourcePort.findAll()).thenReturn(List.of(entry));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(entry.id(), "antenas_flp.csv"))
                .thenReturn(Optional.of(completedRun));
        when(completedRun.getState()).thenReturn(IngestionState.COMPLETED);

        var results = orchestrator.executeAll().join();

        assertThat(results).hasSize(1);
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
