package com.appbit.geoanalytics.application.sources.in;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.application.sources.SourceDTO;
import com.appbit.geoanalytics.application.sources.SourcesResponse;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetSourcesServiceTest {

    private final DataSourcePort dataSourcePort = mock(DataSourcePort.class);
    private final IngestionRunPort ingestionRunPort = mock(IngestionRunPort.class);
    private final GetSourcesService service = new GetSourcesService(dataSourcePort, ingestionRunPort);

    @Test
    void shouldReturnSourcesWithLatestIngestion() {
        var sourceId = UUID.randomUUID();
        var fileName = new SourceFileName("antenas_flp.csv");
        var source = new SourceCatalogEntry(
                sourceId, "V\u00edsent CDRView - Antennas", fileName,
                DataSourceType.SYNTHETIC_DATASET, "Antenas y coordenadas", null, null, null, null);
        var startedAt = Instant.parse("2026-07-04T00:00:00Z");
        var finishedAt = Instant.parse("2026-07-04T00:01:00Z");
        var run = mock(IngestionRun.class);

        when(dataSourcePort.findAll()).thenReturn(List.of(source));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(sourceId, "antenas_flp.csv"))
                .thenReturn(Optional.of(run));
        when(run.getState()).thenReturn(IngestionState.COMPLETED);
        when(run.getStartedAt()).thenReturn(startedAt);
        when(run.getFinishedAt()).thenReturn(finishedAt);
        when(run.getRowsRead()).thenReturn(132L);
        when(run.getRowsInserted()).thenReturn(132L);
        when(run.getRowsRejected()).thenReturn(0L);
        when(run.getErrorMessage()).thenReturn("");

        SourcesResponse result = service.execute();

        assertThat(result.sources()).hasSize(1);
        SourceDTO dto = result.sources().getFirst();
        assertThat(dto.sourceName()).isEqualTo("V\u00edsent CDRView - Antennas");
        assertThat(dto.fileName()).isEqualTo("antenas_flp.csv");
        assertThat(dto.sourceType()).isEqualTo("SYNTHETIC_DATASET");
        assertThat(dto.lastIngestionState()).isEqualTo("COMPLETED");
        assertThat(dto.lastIngestionStartedAt()).isEqualTo(startedAt);
        assertThat(dto.lastIngestionFinishedAt()).isEqualTo(finishedAt);
        assertThat(dto.rowsRead()).isEqualTo(132L);
        assertThat(dto.rowsInserted()).isEqualTo(132L);
        assertThat(dto.rowsRejected()).isEqualTo(0L);
        assertThat(dto.errorMessage()).isEmpty();
    }

    @Test
    void shouldReturnPendingWhenNoIngestionRunExists() {
        var sourceId = UUID.randomUUID();
        var source = new SourceCatalogEntry(
                sourceId, "Not Ingested", new SourceFileName("pending.csv"),
                DataSourceType.SEED_DATA, "Not yet ingested", null, null, null, null);

        when(dataSourcePort.findAll()).thenReturn(List.of(source));
        when(ingestionRunPort.findLatestBySourceIdAndFileName(sourceId, "pending.csv"))
                .thenReturn(Optional.empty());

        SourcesResponse result = service.execute();

        assertThat(result.sources()).hasSize(1);
        SourceDTO dto = result.sources().getFirst();
        assertThat(dto.lastIngestionState()).isEqualTo("PENDING");
        assertThat(dto.lastIngestionStartedAt()).isNull();
        assertThat(dto.lastIngestionFinishedAt()).isNull();
        assertThat(dto.rowsRead()).isZero();
        assertThat(dto.rowsInserted()).isZero();
        assertThat(dto.rowsRejected()).isZero();
        assertThat(dto.errorMessage()).isNull();
    }

    @Test
    void shouldReturnEmptyListWhenNoSources() {
        when(dataSourcePort.findAll()).thenReturn(List.of());

        SourcesResponse result = service.execute();

        assertThat(result.sources()).isEmpty();
    }

    @Test
    void shouldCallDataSourcePort() {
        when(dataSourcePort.findAll()).thenReturn(List.of());

        service.execute();

        verify(dataSourcePort).findAll();
    }
}
