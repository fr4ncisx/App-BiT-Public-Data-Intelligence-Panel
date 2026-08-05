package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegionResolverTest {

    @Mock private RegionJpaRepository regionRepository;

    private RegionResolver regionResolver;
    private static final UUID REGION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        regionResolver = new RegionResolver(regionRepository);
    }

    private RegionEntity region(String clusterName, String municipality) {
        return RegionEntity.builder()
                .id(REGION_ID)
                .clusterName(clusterName)
                .municipality(municipality)
                .build();
    }

    @Test
    void shouldLoadRegionsOnlyOnce() {
        when(regionRepository.findAll())
                .thenReturn(List.of(region("CBD_BEIRAMAR", "Florianopolis")));

        var first = regionResolver.regions();
        var second = regionResolver.regions();

        assertThat(regionResolver.loadCount()).isEqualTo(1);
        assertThat(first).isSameAs(second);
        assertThat(first.byClusterName("CBD_BEIRAMAR"))
                .contains(new RegionRef(REGION_ID, "CBD_BEIRAMAR", "Florianopolis"));
    }

    @Test
    void shouldIndexByTrimmedClusterName() {
        when(regionRepository.findAll())
                .thenReturn(List.of(region("  CBD_BEIRAMAR  ", "Florianopolis")));

        var index = regionResolver.regions();

        assertThat(index.byClusterName("CBD_BEIRAMAR")).isPresent();
        assertThat(index.byClusterName("  CBD_BEIRAMAR  ")).isPresent();
        assertThat(index.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownCluster() {
        when(regionRepository.findAll())
                .thenReturn(List.of(region("CENTRO", "Florianopolis")));

        var index = regionResolver.regions();

        assertThat(index.byClusterName("UNKNOWN")).isEmpty();
    }
}