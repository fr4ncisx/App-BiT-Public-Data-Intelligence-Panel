package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionOutcome;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class IngestionOrchestratorService implements IngestionOrchestrator {

    private static final String FILE_NOT_FOUND_MESSAGE = "File not found in storage";

    private static final Map<String, Set<String>> PREREQUISITES = Map.of(
            "tensor_concentracao.csv", Set.of("antenas_flp.csv"),
            "tensor_fluxo_vias.csv", Set.of("antenas_flp.csv"),
            "tensor_od.csv", Set.of("antenas_flp.csv"),
            "tensor_tempo_deslocamento.csv", Set.of("antenas_flp.csv")
    );

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
        var completed = new HashMap<String, IngestionTaskResult>();
        var remaining = new ArrayList<SourceCatalogEntry>();

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

            remaining.add(ds);
        }

        while (!remaining.isEmpty()) {
            var ready = remaining.stream()
                    .filter(ds -> prerequisitesDone(ds.fileName().value(), completed, remaining))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (ready.isEmpty()) {
                var stuck = remaining.stream()
                        .map(ds -> ds.fileName().value())
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException("Could not progress ingestion; unmet prerequisites for: " + stuck);
            }

            var waveFutures = ready.stream()
                    .map(ds -> processSource(serviceMap.get(ds.fileName().value()), ds.id(), ds.fileName().value()))
                    .toList();

            var waveResults = CompletableFuture.allOf(waveFutures.toArray(CompletableFuture[]::new))
                    .thenApply(_ -> waveFutures.stream().map(CompletableFuture::join).toList())
                    .join();

            for (var i = 0; i < ready.size(); i++) {
                completed.put(ready.get(i).fileName().value(), waveResults.get(i));
            }

            remaining.removeAll(ready);
        }

        return CompletableFuture.completedFuture(dataSources.stream()
                .map(ds -> completed.get(ds.fileName().value()))
                .filter(Objects::nonNull)
                .toList());
    }

    private boolean prerequisitesDone(String fileName, Map<String, IngestionTaskResult> completed, List<SourceCatalogEntry> remaining) {
        var prerequisites = PREREQUISITES.getOrDefault(fileName, Set.of());
        return prerequisites.stream()
                .allMatch(prerequisite -> completed.containsKey(prerequisite)
                        || remaining.stream().noneMatch(ds -> ds.fileName().value().equals(prerequisite)));
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