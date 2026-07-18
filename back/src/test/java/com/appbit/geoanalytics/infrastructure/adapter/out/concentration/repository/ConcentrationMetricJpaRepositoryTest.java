package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.entity.ConcentrationMetricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConcentrationMetricJpaRepositoryTest {

    @Autowired
    private ConcentrationMetricJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = ConcentrationMetricEntity.builder()
                .id(id)
                .sourceId(UUID.randomUUID())
                .regionId(UUID.randomUUID())
                .ecgi("1234567890123")
                .clusterName("Cluster A")
                .municipality("Florianopolis")
                .dayDate(LocalDate.now())
                .period("MANHA")
                .activeUsers(100L)
                .sessions(200L)
                .downloadBytes(1000L)
                .uploadBytes(500L)
                .averageSessionDurationSeconds(120)
                .averageDropRate(new BigDecimal("0.010000"))
                .averageCongestion(new BigDecimal("0.050000"))
                .totalCalls(50)
                .totalMessages(30)
                .latitude(new BigDecimal("-27.595400"))
                .longitude(new BigDecimal("-48.548000"))
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getClusterName()).isEqualTo("Cluster A");
    }
}
