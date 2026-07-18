package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.OriginDestinationFlowEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OriginDestinationFlowJpaRepositoryTest {

    @Autowired
    private OriginDestinationFlowJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = OriginDestinationFlowEntity.builder()
                .id(id)
                .sourceId(UUID.randomUUID())
                .originRegionId(UUID.randomUUID())
                .destinationRegionId(UUID.randomUUID())
                .originClusterName("Cluster A")
                .destinationClusterName("Cluster B")
                .originMunicipality("Florianopolis")
                .destinationMunicipality("Florianopolis")
                .originLatitude(new BigDecimal("-27.595400"))
                .originLongitude(new BigDecimal("-48.548000"))
                .destinationLatitude(new BigDecimal("-27.596800"))
                .destinationLongitude(new BigDecimal("-48.549200"))
                .sameCluster(false)
                .usersCount(100L)
                .tripsCount(150L)
                .averageDistanceKm(new BigDecimal("5.300"))
                .predominantPeriod("MANHA")
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getOriginClusterName()).isEqualTo("Cluster A");
    }
}
