package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionMetricsService {

    private final MeterRegistry meterRegistry;

    private Counter rowsReadCounter;
    private Counter rowsInsertedCounter;
    private Counter rowsRejectedCounter;
    private Timer ingestionTimer;

    @PostConstruct
    void init() {
        rowsReadCounter = meterRegistry.counter("ingestion.rows.read");
        rowsInsertedCounter = meterRegistry.counter("ingestion.rows.inserted");
        rowsRejectedCounter = meterRegistry.counter("ingestion.rows.rejected");
        ingestionTimer = meterRegistry.timer("ingestion.duration");
    }

    public void recordIngestion(long rowsRead, long rowsInserted, long rowsRejected, long durationMillis) {
        rowsReadCounter.increment(rowsRead);
        rowsInsertedCounter.increment(rowsInserted);
        rowsRejectedCounter.increment(rowsRejected);
        ingestionTimer.record(java.time.Duration.ofMillis(durationMillis));
    }
}
