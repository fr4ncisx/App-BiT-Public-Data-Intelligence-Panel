package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.adapter;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity.IngestionRunEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.repository.IngestionRunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IngestionRunRepositoryAdapter implements IngestionRunPort {

    private final IngestionRunJpaRepository repository;

    @Override
    public IngestionRun save(IngestionRun run) {
        Optional<IngestionRunEntity> existingOpt = repository.findById(run.getId().value());
        IngestionRunEntity.IngestionRunEntityBuilder builder = IngestionRunEntity.builder()
                .id(run.getId().value())
                .sourceId(run.getSourceId().value())
                .fileName(run.getFileName().value())
                .status(run.getState().name())
                .rowsRead(run.getRowsRead())
                .rowsInserted(run.getRowsInserted())
                .rowsRejected(run.getRowsRejected())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .errorMessage(run.getErrorMessage());

        existingOpt.ifPresent(existing -> builder.version(existing.getVersion()));

        repository.save(builder.build());
        return run;
    }

    @Override
    public Optional<IngestionRun> findLatestBySourceIdAndFileName(UUID sourceId, String fileName) {
        return repository.findFirstBySourceIdAndFileNameOrderByStartedAtDesc(sourceId, fileName)
                .map(this::toDomain);
    }

    private IngestionRun toDomain(IngestionRunEntity entity) {
        return IngestionRun.builder()
                .id(new IngestionRunId(entity.getId()))
                .sourceId(new DataSourceId(entity.getSourceId()))
                .fileName(new SourceFileName(entity.getFileName()))
                .state(IngestionState.valueOf(entity.getStatus()))
                .rowsRead(entity.getRowsRead())
                .rowsInserted(entity.getRowsInserted())
                .rowsRejected(entity.getRowsRejected())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .errorMessage(entity.getErrorMessage())
                .build();
    }
}
