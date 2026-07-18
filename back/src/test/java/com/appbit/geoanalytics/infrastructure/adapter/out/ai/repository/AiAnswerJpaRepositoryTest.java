package com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiAnswerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AiAnswerJpaRepositoryTest {

    @Autowired
    private AiAnswerJpaRepository repository;

    @Test
    void shouldInsertAndFind() {
        var id = UUID.randomUUID();
        var queryId = UUID.randomUUID();
        var entity = AiAnswerEntity.builder()
                .id(id)
                .queryId(queryId)
                .summary("Short summary")
                .explanation("Detailed explanation goes here.")
                .data("[]")
                .suggestedVisualization("MAP")
                .confidenceLevel("HIGH")
                .createdAt(Instant.now())
                .evidence(List.of("evidence 1", "evidence 2"))
                .regionIds(List.of(UUID.randomUUID()))
                .sourceIds(List.of(UUID.randomUUID()))
                .warnings(List.of("warning 1"))
                .build();

        repository.saveAndFlush(entity);

        var found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getSummary()).isEqualTo("Short summary");
        assertThat(found.get().getExplanation()).isEqualTo("Detailed explanation goes here.");
        assertThat(found.get().getData()).isEqualTo("[]");
        assertThat(found.get().getSuggestedVisualization()).isEqualTo("MAP");
        assertThat(found.get().getConfidenceLevel()).isEqualTo("HIGH");
        assertThat(found.get().getEvidence()).containsExactly("evidence 1", "evidence 2");
        assertThat(found.get().getRegionIds()).hasSize(1);
        assertThat(found.get().getSourceIds()).hasSize(1);
        assertThat(found.get().getWarnings()).containsExactly("warning 1");
    }
}
