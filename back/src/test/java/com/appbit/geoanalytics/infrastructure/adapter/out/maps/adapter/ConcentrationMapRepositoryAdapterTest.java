package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository.ConcentrationMetricJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcentrationMapRepositoryAdapterTest {

    @Mock
    private ConcentrationMetricJpaRepository concentrationMetricJpaRepository;

    @InjectMocks
    private ConcentrationMapRepositoryAdapter adapter;

    private final UUID regionId = UUID.randomUUID();

    @Test
    void shouldAggregateConcentrationByRegion() {
        when(concentrationMetricJpaRepository.findConcentrationAggregateByRegionIds(List.of(regionId)))
                .thenReturn(Collections.singletonList(new Object[]{regionId, 300L, 0.25}));

        List<ConcentrationSummary> result = adapter.findByRegionIds(List.of(regionId), null);

        assertThat(result).hasSize(1);
        ConcentrationSummary summary = result.getFirst();
        assertThat(summary.regionId()).isEqualTo(regionId);
        assertThat(summary.totalActiveUsers()).isEqualTo(300L);
        assertThat(summary.avgCongestion()).isEqualByComparingTo("0.25");
    }

    @Test
    void shouldFilterByPeriod() {
        when(concentrationMetricJpaRepository.findConcentrationAggregateByRegionIdsAndPeriod(
                List.of(regionId), "MANHA"))
                .thenReturn(Collections.singletonList(new Object[]{regionId, 100L, 0.30}));

        List<ConcentrationSummary> result = adapter.findByRegionIds(List.of(regionId), "MANHA");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalActiveUsers()).isEqualTo(100L);
    }

    @Test
    void shouldReturnEmptyWhenRegionNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(concentrationMetricJpaRepository.findConcentrationAggregateByRegionIds(List.of(unknownId)))
                .thenReturn(Collections.emptyList());

        List<ConcentrationSummary> result = adapter.findByRegionIds(List.of(unknownId), null);

        assertThat(result).isEmpty();
    }
}
