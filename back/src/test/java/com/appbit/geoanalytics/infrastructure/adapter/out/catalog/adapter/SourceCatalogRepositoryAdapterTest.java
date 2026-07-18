package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceCatalogRepositoryAdapterTest {

    @Mock
    private DataSourceJpaRepository dataSourceJpaRepository;

    @InjectMocks
    private SourceCatalogRepositoryAdapter adapter;

    @Test
    void shouldReturnAllSources() {
        var entity = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Vísent CDRView - Antennas")
                .fileName("antenas_flp.csv")
                .sourceType("SYNTHETIC_DATASET")
                .description("Antenas, coordenadas, municipio e cluster.")
                .createdAt(Instant.now())
                .build();

        when(dataSourceJpaRepository.findAll()).thenReturn(List.of(entity));

        List<SourceSummaryDTO> result = adapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Vísent CDRView - Antennas");
        assertThat(result.getFirst().file()).isEqualTo("antenas_flp.csv");
        assertThat(result.getFirst().sourceType()).isEqualTo("SYNTHETIC_DATASET");
    }

    @Test
    void shouldReturnEmptyWhenNoSources() {
        when(dataSourceJpaRepository.findAll()).thenReturn(List.of());

        List<SourceSummaryDTO> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
