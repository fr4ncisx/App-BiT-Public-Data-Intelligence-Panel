package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.TravelDistanceMetricEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TravelDistanceMetricJpaRepositoryTest {

    @Autowired
    private TravelDistanceMetricJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = TravelDistanceMetricEntity.builder()
                .id(id)
                .sourceId(UUID.randomUUID())
                .originRegionId(UUID.randomUUID())
                .destinationRegionId(UUID.randomUUID())
                .originClusterName("Cluster A")
                .destinationClusterName("Cluster B")
                .sameCluster(false)
                .observations(35L)
                .averageDistanceKm(new BigDecimal("5.300"))
                .p25DistanceKm(new BigDecimal("2.100"))
                .p75DistanceKm(new BigDecimal("8.500"))
                .predominantPeriod("MANHA")
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getObservations()).isEqualTo(35L);
    }
}
