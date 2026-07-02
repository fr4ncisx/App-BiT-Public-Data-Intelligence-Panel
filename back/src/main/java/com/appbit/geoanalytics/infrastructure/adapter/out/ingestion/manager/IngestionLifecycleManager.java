package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IngestionLifecycleManager {

    private final IngestionRunPort ingestionRunPort;
    private final IdGeneratorPort idGeneratorPort;
    private final TransactionTemplate transactionTemplate;

    public IngestionRun start(String fileName, UUID sourceId) {
        var run = IngestionRun.builder()
                .id(new IngestionRunId(idGeneratorPort.generate()))
                .sourceId(new DataSourceId(sourceId))
                .fileName(new SourceFileName(fileName))
                .state(IngestionState.RUNNING)
                .startedAt(Instant.now())
                .build();

        return ingestionRunPort.save(run);
    }

    public void complete(IngestionRun run, long read, long inserted, long rejected) {
        if (read > 0) run.registerReadRows(read);
        if (inserted > 0) run.registerInsertedRows(inserted);
        if (rejected > 0) run.registerRejectedRows(rejected);

        run.complete(Instant.now());
        ingestionRunPort.save(run);
    }

    public void fail(IngestionRun run, Exception e) {
        transactionTemplate.executeWithoutResult(_ -> {
            var errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            run.fail(Instant.now(), errorMessage);
            ingestionRunPort.save(run);
        });
    }
}
