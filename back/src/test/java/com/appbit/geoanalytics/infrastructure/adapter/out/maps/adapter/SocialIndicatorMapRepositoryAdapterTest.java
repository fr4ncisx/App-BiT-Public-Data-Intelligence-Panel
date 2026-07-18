package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialIndicatorMapRepositoryAdapterTest {

    @Mock
    private SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @InjectMocks
    private SocialIndicatorMapRepositoryAdapter adapter;

    private final UUID regionId = UUID.randomUUID();

    @Test
    void shouldReturnAllIndicatorsForRegion() {
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(new Object[]{regionId, "TRAINING", BigDecimal.valueOf(3), "programs"});
        mockData.add(new Object[]{regionId, "EMPLOYABILITY", BigDecimal.valueOf(0.85), "index"});

        when(socialIndicatorJpaRepository.findSocialIndicatorsByRegionIds(List.of(regionId)))
                .thenReturn(mockData);

        List<SocialIndicatorSummary> result = adapter.findByRegionIds(List.of(regionId), null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SocialIndicatorSummary::indicatorType)
                .containsExactlyInAnyOrder("TRAINING", "EMPLOYABILITY");
    }

    @Test
    void shouldFilterByIndicatorType() {
        List<Object[]> mockFiltered = new ArrayList<>();
        mockFiltered.add(new Object[]{regionId, "TRAINING", BigDecimal.valueOf(3), "programs"});

        when(socialIndicatorJpaRepository.findSocialIndicatorsByRegionIdsAndType(
                List.of(regionId), "TRAINING"))
                .thenReturn(mockFiltered);

        List<SocialIndicatorSummary> result = adapter.findByRegionIds(List.of(regionId), "TRAINING");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().indicatorType()).isEqualTo("TRAINING");
        assertThat(result.getFirst().score()).isEqualByComparingTo("3");
        assertThat(result.getFirst().unit()).isEqualTo("programs");
    }

    @Test
    void shouldReturnEmptyWhenRegionNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(socialIndicatorJpaRepository.findSocialIndicatorsByRegionIds(List.of(unknownId)))
                .thenReturn(Collections.emptyList());

        List<SocialIndicatorSummary> result = adapter.findByRegionIds(List.of(unknownId), null);

        assertThat(result).isEmpty();
    }
}
