package com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiQueryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AiQueryJpaRepositoryTest {

    @Autowired
    private AiQueryJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var entity = AiQueryEntity.builder()
                .id(id)
                .requestId(UUID.randomUUID())
                .queryText("test query")
                .language("pt")
                .intent("TRAINING_GAP")
                .filters("{}")
                .status("COMPLETED")
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getQueryText()).isEqualTo("test query");
    }
}
