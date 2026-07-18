package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionCatalogRepositoryAdapterTest {

    @Mock
    private RegionJpaRepository regionJpaRepository;

    @InjectMocks
    private RegionCatalogRepositoryAdapter adapter;

    @Test
    void shouldReturnAllRegions() {
        var entity = RegionEntity.builder()
                .id(UUID.randomUUID())
                .regionCode("TRINDADE")
                .regionName("Trindade")
                .municipality("Florianopolis")
                .clusterName("Trindade")
                .centerLatitude(new BigDecimal("-27.5954"))
                .centerLongitude(new BigDecimal("-48.5480"))
                .createdAt(Instant.now())
                .build();

        when(regionJpaRepository.findAll()).thenReturn(List.of(entity));

        List<RegionSummaryDTO> result = adapter.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().regionCode()).isEqualTo("TRINDADE");
        assertThat(result.getFirst().regionName()).isEqualTo("Trindade");
        assertThat(result.getFirst().municipality()).isEqualTo("Florianopolis");
    }

    @Test
    void shouldReturnEmptyWhenNoRegions() {
        when(regionJpaRepository.findAll()).thenReturn(List.of());

        List<RegionSummaryDTO> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
