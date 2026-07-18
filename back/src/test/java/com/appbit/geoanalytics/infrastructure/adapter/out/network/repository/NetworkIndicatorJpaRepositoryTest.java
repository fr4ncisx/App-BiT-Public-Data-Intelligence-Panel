package com.appbit.geoanalytics.infrastructure.adapter.out.network.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.network.entity.NetworkIndicatorEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NetworkIndicatorJpaRepositoryTest {

    @Autowired
    private NetworkIndicatorJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = NetworkIndicatorEntity.builder()
                .id(id)
                .regionId(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .indicatorType("COVERAGE")
                .score(new BigDecimal("0.850000"))
                .unit("SCORE")
                .gapLevel("LOW")
                .confidenceLevel("HIGH")
                .period("MANHA")
                .description("Network coverage indicator")
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getIndicatorType()).isEqualTo("COVERAGE");
    }
}
