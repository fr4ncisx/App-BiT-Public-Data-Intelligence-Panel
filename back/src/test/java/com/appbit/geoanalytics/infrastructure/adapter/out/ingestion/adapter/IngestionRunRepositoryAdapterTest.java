package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.adapter;

import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.domain.testing.DomainFixtures;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity.IngestionRunEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.repository.IngestionRunJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionRunRepositoryAdapterTest {

    @Mock
    private IngestionRunJpaRepository repository;

    @InjectMocks
    private IngestionRunRepositoryAdapter adapter;

    @Test
    void shouldSaveIngestionRunAndReturnIt() {
        UUID id = DomainFixtures.uuidV7();
        UUID sourceId = DomainFixtures.uuidV7();
        Instant startedAt = Instant.now();

        IngestionRun run = IngestionRun.builder()
                .id(new IngestionRunId(id))
                .sourceId(new DataSourceId(sourceId))
                .fileName(new SourceFileName("antenas_flp.csv"))
                .state(IngestionState.RUNNING)
                .startedAt(startedAt)
                .build();

        when(repository.save(org.mockito.ArgumentMatchers.any(IngestionRunEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IngestionRun result = adapter.save(run);

        assertThat(result).isSameAs(run);

        ArgumentCaptor<IngestionRunEntity> captor = ArgumentCaptor.forClass(IngestionRunEntity.class);
        verify(repository).save(captor.capture());

        IngestionRunEntity saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(id);
        assertThat(saved.getSourceId()).isEqualTo(sourceId);
        assertThat(saved.getFileName()).isEqualTo("antenas_flp.csv");
        assertThat(saved.getStatus()).isEqualTo("RUNNING");
        assertThat(saved.getStartedAt()).isEqualTo(startedAt);
        assertThat(saved.getRowsRead()).isEqualTo(0);
        assertThat(saved.getRowsInserted()).isEqualTo(0);
        assertThat(saved.getRowsRejected()).isEqualTo(0);
    }

    @Test
    void shouldMapAllFieldsWhenRunHasCompletedState() {
        UUID id = DomainFixtures.uuidV7();
        UUID sourceId = DomainFixtures.uuidV7();
        Instant startedAt = Instant.now();
        Instant finishedAt = Instant.now().plusSeconds(30);

        IngestionRun run = IngestionRun.builder()
                .id(new IngestionRunId(id))
                .sourceId(new DataSourceId(sourceId))
                .fileName(new SourceFileName("tensor_concentracao.csv"))
                .state(IngestionState.RUNNING)
                .startedAt(startedAt)
                .build();
        run.registerReadRows(100);
        run.registerInsertedRows(95);
        run.registerRejectedRows(5);
        run.complete(finishedAt);

        when(repository.save(org.mockito.ArgumentMatchers.any(IngestionRunEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IngestionRun result = adapter.save(run);

        assertThat(result).isSameAs(run);

        ArgumentCaptor<IngestionRunEntity> captor = ArgumentCaptor.forClass(IngestionRunEntity.class);
        verify(repository).save(captor.capture());

        IngestionRunEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("COMPLETED");
        assertThat(saved.getFinishedAt()).isEqualTo(finishedAt);
        assertThat(saved.getRowsRead()).isEqualTo(100);
        assertThat(saved.getRowsInserted()).isEqualTo(95);
        assertThat(saved.getRowsRejected()).isEqualTo(5);
    }

    @Test
    void shouldMapFailedState() {
        UUID id = DomainFixtures.uuidV7();
        UUID sourceId = DomainFixtures.uuidV7();
        Instant startedAt = Instant.now();
        Instant finishedAt = Instant.now().plusSeconds(5);

        IngestionRun run = IngestionRun.builder()
                .id(new IngestionRunId(id))
                .sourceId(new DataSourceId(sourceId))
                .fileName(new SourceFileName("social_indicators_seed.csv"))
                .state(IngestionState.FAILED)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .errorMessage("Storage unavailable")
                .build();

        when(repository.save(org.mockito.ArgumentMatchers.any(IngestionRunEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(run);

        ArgumentCaptor<IngestionRunEntity> captor = ArgumentCaptor.forClass(IngestionRunEntity.class);
        verify(repository).save(captor.capture());

        IngestionRunEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(saved.getErrorMessage()).isEqualTo("Storage unavailable");
    }

    @Test
    void shouldReturnEmptyWhenNoRunExists() {
        UUID sourceId = DomainFixtures.uuidV7();
        when(repository.findFirstBySourceIdAndFileNameOrderByStartedAtDesc(sourceId, "antenas_flp.csv")).thenReturn(java.util.Optional.empty());

        var result = adapter.findLatestBySourceIdAndFileName(sourceId, "antenas_flp.csv");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapEntityToDomainWhenRunExists() {
        UUID id = DomainFixtures.uuidV7();
        UUID sourceId = DomainFixtures.uuidV7();
        Instant startedAt = Instant.now();
        Instant finishedAt = Instant.now().plusSeconds(30);

        IngestionRunEntity entity = IngestionRunEntity.builder()
                .id(id)
                .sourceId(sourceId)
                .fileName("antenas_flp.csv")
                .status("COMPLETED")
                .rowsRead(100)
                .rowsInserted(95)
                .rowsRejected(5)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();

        when(repository.findFirstBySourceIdAndFileNameOrderByStartedAtDesc(sourceId, "antenas_flp.csv"))
                .thenReturn(java.util.Optional.of(entity));

        var result = adapter.findLatestBySourceIdAndFileName(sourceId, "antenas_flp.csv");

        assertThat(result).isPresent();
        var run = result.get();
        assertThat(run.getId().value()).isEqualTo(id);
        assertThat(run.getSourceId().value()).isEqualTo(sourceId);
        assertThat(run.getState()).isEqualTo(IngestionState.COMPLETED);
        assertThat(run.getRowsRead()).isEqualTo(100);
        assertThat(run.getRowsInserted()).isEqualTo(95);
        assertThat(run.getRowsRejected()).isEqualTo(5);
        assertThat(run.getStartedAt()).isEqualTo(startedAt);
        assertThat(run.getFinishedAt()).isEqualTo(finishedAt);
    }

    @Test
    void shouldCopyVersionFromExistingEntityWhenSaving() {
        UUID id = DomainFixtures.uuidV7();
        UUID sourceId = DomainFixtures.uuidV7();
        Instant startedAt = Instant.now();

        IngestionRun run = IngestionRun.builder()
                .id(new IngestionRunId(id))
                .sourceId(new DataSourceId(sourceId))
                .fileName(new SourceFileName("antenas_flp.csv"))
                .state(IngestionState.RUNNING)
                .startedAt(startedAt)
                .build();

        IngestionRunEntity existingEntity = IngestionRunEntity.builder()
                .id(id)
                .sourceId(sourceId)
                .fileName("antenas_flp.csv")
                .status("RUNNING")
                .startedAt(startedAt)
                .version(42L)
                .build();

        when(repository.findById(id)).thenReturn(java.util.Optional.of(existingEntity));
        when(repository.save(org.mockito.ArgumentMatchers.any(IngestionRunEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        adapter.save(run);

        ArgumentCaptor<IngestionRunEntity> captor = ArgumentCaptor.forClass(IngestionRunEntity.class);
        verify(repository).save(captor.capture());

        IngestionRunEntity saved = captor.getValue();
        assertThat(saved.getVersion()).isEqualTo(42L);
    }
}
