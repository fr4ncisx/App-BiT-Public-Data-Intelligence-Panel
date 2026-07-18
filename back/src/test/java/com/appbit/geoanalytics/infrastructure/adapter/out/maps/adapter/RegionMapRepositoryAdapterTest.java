package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
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
class RegionMapRepositoryAdapterTest {

    @Mock
    private RegionJpaRepository regionJpaRepository;

    @InjectMocks
    private RegionMapRepositoryAdapter adapter;

    @Test
    void shouldReturnAllRegionsWithGeoPoints() {
        UUID id = UUID.randomUUID();
        var entity = RegionEntity.builder()
                .id(id)
                .regionCode("CBD_BEIRAMAR")
                .regionName("CBD Beiramar")
                .municipality("Florianopolis")
                .clusterName("Beiramar")
                .centerLatitude(new BigDecimal("-27.5954"))
                .centerLongitude(new BigDecimal("-48.5480"))
                .createdAt(Instant.now())
                .build();

        when(regionJpaRepository.findAll()).thenReturn(List.of(entity));

        List<RegionMapDTO> result = adapter.findAll();

        assertThat(result).hasSize(1);
        RegionMapDTO dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.regionCode()).isEqualTo("CBD_BEIRAMAR");
        assertThat(dto.regionName()).isEqualTo("CBD Beiramar");
        assertThat(dto.municipality()).isEqualTo("Florianopolis");
        assertThat(dto.geoPoint().lat()).isEqualByComparingTo("-27.5954");
        assertThat(dto.geoPoint().lng()).isEqualByComparingTo("-48.5480");
        assertThat(dto.indicators()).isNull();
    }

    @Test
    void shouldReturnEmptyWhenNoRegions() {
        when(regionJpaRepository.findAll()).thenReturn(List.of());

        List<RegionMapDTO> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
