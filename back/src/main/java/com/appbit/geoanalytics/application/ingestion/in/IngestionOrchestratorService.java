package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionOutcome;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class IngestionOrchestratorService implements IngestionOrchestrator {

    private static final String FILE_NOT_FOUND_MESSAGE = "File not found in storage";

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final IngestionRunPort ingestionRunPort;
    private final List<CsvIngestService> csvIngestServices;
    private final RetryPolicy retryPolicy;
    private final Set<String> skippedFileNames;
    private final Executor taskExecutor;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<IngestionTaskResult>> executeAll() {
        var serviceMap = buildServiceMap();

        var dataSources = dataSourcePort.findAll();
        var futures = new java.util.ArrayList<CompletableFuture<IngestionTaskResult>>(dataSources.size());

        for (var ds : dataSources) {
            var fileName = ds.fileName().value();
            var service = serviceMap.get(fileName);

            if (service == null) {
                if (skippedFileNames.contains(fileName)) {
                    log.debug("No CsvIngestService registered for file '{}'; source '{}' skipped by design",
                            fileName, ds.sourceName());
                } else {
                    log.warn("No CsvIngestService registered for file '{}'; source '{}' skipped",
                            fileName, ds.sourceName());
                }
                continue;
            }

            futures.add(processSource(service, ds.id(), fileName));
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        var all = futures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(all)
                .thenApply(_ -> futures.stream().map(CompletableFuture::join).toList());
    }

    private CompletableFuture<IngestionTaskResult> processSource(
            CsvIngestService service,
            UUID sourceId,
            String fileName
    ) {
        var lastRun = ingestionRunPort.findLatestBySourceIdAndFileName(sourceId, fileName);
        if (lastRun.isPresent() && lastRun.get().getState() == IngestionState.COMPLETED) {
            return CompletableFuture.completedFuture(new IngestionTaskResult(
                    fileName, true, IngestionOutcome.ALREADY_INGESTED, null, 0, 0, 0));
        }

        return attempt(service, fileName, 1);
    }

    private CompletableFuture<IngestionTaskResult> attempt(CsvIngestService service, String fileName, int attempt) {
        return CompletableFuture.supplyAsync(() -> tryIngest(service, fileName), taskExecutor)
                .handle((result, error) -> error == null
                        ? CompletableFuture.completedFuture(result)
                        : scheduleRetry(service, fileName, attempt, error))
                .thenCompose(Function.identity());
    }

    private CompletableFuture<IngestionTaskResult> scheduleRetry(
            CsvIngestService service,
            String fileName,
            int attempt,
            Throwable error
    ) {
        log.warn("Ingestion attempt {}/{} failed for {}: {}",
                attempt, retryPolicy.maxAttempts(), fileName, error.getMessage());

        if (attempt >= retryPolicy.maxAttempts()) {
            return CompletableFuture.completedFuture(new IngestionTaskResult(
                    fileName, false, IngestionOutcome.FAILED, error.getMessage(), 0, 0, 0));
        }

        var delayMillis = retryPolicy.baseDelayMillis() * attempt;
        var delayedExecutor = CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS, taskExecutor);
        return CompletableFuture.supplyAsync(() -> null, delayedExecutor)
                .thenCompose(_ -> attempt(service, fileName, attempt + 1));
    }

    private IngestionTaskResult tryIngest(CsvIngestService service, String fileName) {
        var key = new DatasetObjectKey(fileName);

        if (!storagePort.exists(key)) {
            return new IngestionTaskResult(
                    fileName, false, IngestionOutcome.SKIPPED, FILE_NOT_FOUND_MESSAGE, 0, 0, 0);
        }

        var result = service.ingest(key);
        return new IngestionTaskResult(fileName, true, IngestionOutcome.INGESTED, null,
                result.rowsRead(), result.rowsInserted(), result.rowsRejected());
    }

    private Map<String, CsvIngestService> buildServiceMap() {
        var serviceMap = new HashMap<String, CsvIngestService>();

        for (var service : csvIngestServices) {
            var fileName = service.supportedFileName();
            var existing = serviceMap.putIfAbsent(fileName, service);

            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate CsvIngestService for file '" + fileName + "': "
                                + existing.getClass().getSimpleName() + " and "
                                + service.getClass().getSimpleName());
            }
        }

        return Map.copyOf(serviceMap);
    }
}