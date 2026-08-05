package com.appbit.geoanalytics.infrastructure.application;

import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Profile("!test")
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final IngestionOrchestrator orchestrator;
    private final ApplicationContext applicationContext;
    private final IngestionProperties ingestionProperties;

    public DataInitializer(IngestionOrchestrator orchestrator, ApplicationContext applicationContext, IngestionProperties ingestionProperties) {
        this.orchestrator = orchestrator;
        this.applicationContext = applicationContext;
        this.ingestionProperties = ingestionProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (applicationContext.getParent() != null) {
            log.info("Skipping CSV ingestion in child management context.");
            return;
        }

        if (!ingestionProperties.enabled()) {
            log.info("CSV ingestion disabled via appbit.ingestion.enabled=false; skipping startup ingestion.");
            return;
        }

        log.info("Starting CSV ingestion from storage...");
        try {
            var results = orchestrator.executeAll()
                    .orTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .join();

            var failed = results.stream()
                    .filter(r -> !r.success())
                    .toList();

            results.forEach(r -> {
                switch (r.outcome()) {
                    case INGESTED -> log.info("Ingested {}: read={}, inserted={}, rejected={}",
                            r.fileName(), r.rowsRead(), r.rowsInserted(), r.rowsRejected());
                    case ALREADY_INGESTED -> log.info("{} — already ingested", r.fileName());
                    case SKIPPED -> log.warn("Skipped {}: {}", r.fileName(), r.errorMessage());
                    case FAILED -> log.warn("Failed to ingest {}: {}", r.fileName(), r.errorMessage());
                }
            });

            if (!failed.isEmpty()) {
                var details = failed.stream()
                        .map(r -> r.fileName() + ": " + errorMessage(r.errorMessage()).replaceAll("[\\p{Cc}]", " "))
                        .collect(Collectors.joining("; "));

                if (ingestionProperties.failFast()) {
                    throw new RuntimeException("CSV ingestion failed: " + details);
                }

                log.warn("CSV ingestion completed with failures (fail-fast disabled): {}", details);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("CSV ingestion failed: " + e.getMessage(), e);
        }
    }

    private String errorMessage(String message) {
        return (message == null || message.isBlank()) ? "Unknown error" : message;
    }
}
