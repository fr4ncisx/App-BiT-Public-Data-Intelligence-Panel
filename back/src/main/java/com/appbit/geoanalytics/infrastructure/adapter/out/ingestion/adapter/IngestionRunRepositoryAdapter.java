package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.adapter;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity.IngestionRunEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.repository.IngestionRunJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionRunRepositoryAdapter implements IngestionRunPort {

    private final IngestionRunJpaRepository repository;

    @Override
    public IngestionRun save(IngestionRun run) {
        IngestionRunEntity entity = IngestionRunEntity.builder()
                .id(run.getId().value())
                .sourceId(run.getSourceId().value())
                .fileName(run.getFileName().value())
                .status(run.getState().name())
                .rowsRead(run.getRowsRead())
                .rowsInserted(run.getRowsInserted())
                .rowsRejected(run.getRowsRejected())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .errorMessage(run.getErrorMessage())
                .build();

        repository.save(entity);
        return run;
    }
}
