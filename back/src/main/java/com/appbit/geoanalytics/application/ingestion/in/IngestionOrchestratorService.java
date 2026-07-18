package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IngestionOrchestratorService implements IngestionOrchestrator {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final IngestionRunPort ingestionRunPort;
    private final List<CsvIngestService> csvIngestServices;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<IngestionTaskResult>> executeAll() {
        var serviceMap = csvIngestServices.stream()
                .collect(Collectors.toMap(CsvIngestService::supportedFileName, Function.identity()));

        var dataSources = dataSourcePort.findAll();
        var results = new ArrayList<IngestionTaskResult>(dataSources.size());

        for (var ds : dataSources) {
            var fileName = ds.fileName().value();
            var service = serviceMap.get(fileName);

            if (service == null) {
                continue;
            }

            var lastRun = ingestionRunPort.findLatestBySourceIdAndFileName(ds.id(), fileName);
            if (lastRun.isPresent() && lastRun.get().getState() == IngestionState.COMPLETED) {
                results.add(IngestionTaskResult.success(fileName, CsvIngestResult.of(0, 0, 0)));
                continue;
            }

            var key = new DatasetObjectKey(fileName);
            results.add(ingestWithRetry(service, key, fileName));
        }

        return CompletableFuture.completedFuture(List.copyOf(results));
    }

    private IngestionTaskResult ingestWithRetry(CsvIngestService service, DatasetObjectKey key, String fileName) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (!storagePort.exists(key)) {
                    return IngestionTaskResult.skipped(fileName, "File not found in storage");
                }

                var result = service.ingest(key);
                return IngestionTaskResult.success(fileName, result);

            } catch (Exception e) {
                log.warn("Ingestion attempt {}/{} failed for {}: {}", attempt, MAX_RETRIES, fileName, e.getMessage());
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return IngestionTaskResult.failed(fileName, "Interrupted during retry");
                    }
                }
            }
        }

        return IngestionTaskResult.failed(fileName, lastException != null ? lastException.getMessage() : "Unknown error");
    }
}
