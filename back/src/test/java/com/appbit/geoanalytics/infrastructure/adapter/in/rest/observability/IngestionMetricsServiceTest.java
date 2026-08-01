package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionMetricsServiceTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final IngestionMetricsService service = new IngestionMetricsService(meterRegistry);

    @Test
    void initRegistersCountersAndTimer() {
        service.init();

        assertThat(meterRegistry.get("ingestion.rows.read").counter()).isNotNull();
        assertThat(meterRegistry.get("ingestion.rows.inserted").counter()).isNotNull();
        assertThat(meterRegistry.get("ingestion.rows.rejected").counter()).isNotNull();
        assertThat(meterRegistry.get("ingestion.duration").timer()).isNotNull();
    }

    @Test
    void recordIngestionIncrementsCountersAndTimer() {
        service.init();

        service.recordIngestion(5, 3, 1, 500);

        assertThat(meterRegistry.get("ingestion.rows.read").counter().count()).isEqualTo(5.0);
        assertThat(meterRegistry.get("ingestion.rows.inserted").counter().count()).isEqualTo(3.0);
        assertThat(meterRegistry.get("ingestion.rows.rejected").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("ingestion.duration").timer().count()).isEqualTo(1L);
        assertThat(meterRegistry.get("ingestion.duration").timer().totalTime(TimeUnit.MILLISECONDS))
                .isEqualTo(500.0);
    }

    @Test
    void recordIngestionAccumulatesAcrossCalls() {
        service.init();

        service.recordIngestion(2, 1, 0, 100);
        service.recordIngestion(3, 2, 1, 200);

        assertThat(meterRegistry.get("ingestion.rows.read").counter().count()).isEqualTo(5.0);
        assertThat(meterRegistry.get("ingestion.rows.inserted").counter().count()).isEqualTo(3.0);
        assertThat(meterRegistry.get("ingestion.rows.rejected").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("ingestion.duration").timer().count()).isEqualTo(2L);
    }
}
