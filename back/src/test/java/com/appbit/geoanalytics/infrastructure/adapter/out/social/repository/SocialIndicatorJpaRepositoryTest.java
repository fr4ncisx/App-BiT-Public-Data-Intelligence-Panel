package com.appbit.geoanalytics.infrastructure.adapter.out.social.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.social.entity.SocialIndicatorEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SocialIndicatorJpaRepositoryTest {

    @Autowired
    private SocialIndicatorJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = SocialIndicatorEntity.builder()
                .id(id)
                .regionId(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .indicatorType("TRAINING")
                .score(new BigDecimal("0.750000"))
                .unit("SCORE")
                .gapLevel("HIGH")
                .confidenceLevel("MEDIUM")
                .description("Training indicator test")
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getIndicatorType()).isEqualTo("TRAINING");
    }
}
