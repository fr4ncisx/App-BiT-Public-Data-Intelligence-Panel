package com.appbit.geoanalytics.infrastructure.application;

import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final IngestionOrchestrator orchestrator;
    private final ApplicationContext applicationContext;

    public DataInitializer(IngestionOrchestrator orchestrator, ApplicationContext applicationContext) {
        this.orchestrator = orchestrator;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (applicationContext.getParent() != null) {
            log.info("Skipping CSV ingestion in child management context.");
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
                if (r.success()) {
                    if (r.rowsRead() == 0 && r.rowsInserted() == 0 && r.rowsRejected() == 0) {
                        log.info("{} — already ingested", r.fileName());
                    } else {
                        log.info("Ingested {}: read={}, inserted={}, rejected={}",
                                r.fileName(), r.rowsRead(), r.rowsInserted(), r.rowsRejected());
                    }
                } else {
                    log.warn("Failed to ingest {}: {}",
                            r.fileName(), r.errorMessage());
                }
            });

            if (!failed.isEmpty()) {
                var details = failed.stream()
                        .map(r -> r.fileName() + ": " + Objects.requireNonNullElse(r.errorMessage(), "Unknown error").replaceAll("[\\p{Cc}]", " "))
                        .collect(Collectors.joining("; "));
                throw new RuntimeException("CSV ingestion failed: " + details);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("CSV ingestion failed: " + e.getMessage(), e);
        }
    }
}
