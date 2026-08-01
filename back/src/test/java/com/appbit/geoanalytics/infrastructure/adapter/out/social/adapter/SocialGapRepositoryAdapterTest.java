package com.appbit.geoanalytics.infrastructure.adapter.out.social.adapter;

import com.appbit.geoanalytics.application.social.out.SocialGapSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialGapRepositoryAdapterTest {

    @Mock
    private SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @InjectMocks
    private SocialGapRepositoryAdapter adapter;

    @Test
    void mapsAllColumnsInOrder() {
        var regionId = UUID.randomUUID();
        Object[] row = {
                regionId,
                "TRAINING",
                new BigDecimal("0.750000"),
                "SCORE",
                "HIGH",
                "MEDIUM",
                "Training gap",
                "SC-CBD",
                "Centro",
                "Florianopolis",
                "SYNTHETIC_DATASET"
        };
        when(socialIndicatorJpaRepository.findSocialGapByIndicatorTypePaginated("TRAINING", 20, 0))
                .thenReturn(List.<Object[]>of(row));

        List<SocialGapSummary> result = adapter.findByIndicatorType("TRAINING", 20, 0);

        assertThat(result).hasSize(1);
        SocialGapSummary summary = result.getFirst();
        assertThat(summary.regionId()).isEqualTo(regionId);
        assertThat(summary.regionCode()).isEqualTo("SC-CBD");
        assertThat(summary.regionName()).isEqualTo("Centro");
        assertThat(summary.municipality()).isEqualTo("Florianopolis");
        assertThat(summary.score()).isEqualTo(new BigDecimal("0.750000"));
        assertThat(summary.unit()).isEqualTo("SCORE");
        assertThat(summary.gapLevel()).isEqualTo("HIGH");
        assertThat(summary.confidenceLevel()).isEqualTo("MEDIUM");
        assertThat(summary.description()).isEqualTo("Training gap");
        assertThat(summary.sourceType()).isEqualTo("SYNTHETIC_DATASET");
    }

    @Test
    void passesIndicatorTypeLimitAndOffsetToRepository() {
        when(socialIndicatorJpaRepository.findSocialGapByIndicatorTypePaginated("EMPLOYABILITY", 50, 100))
                .thenReturn(List.of());

        adapter.findByIndicatorType("EMPLOYABILITY", 50, 100);

        verify(socialIndicatorJpaRepository)
                .findSocialGapByIndicatorTypePaginated("EMPLOYABILITY", 50, 100);
    }

    @Test
    void returnsEmptyWhenRepositoryReturnsNoRows() {
        when(socialIndicatorJpaRepository.findSocialGapByIndicatorTypePaginated("MENTORSHIP", 20, 0))
                .thenReturn(List.of());

        List<SocialGapSummary> result = adapter.findByIndicatorType("MENTORSHIP", 20, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void mapsMultipleRowsPreservingOrder() {
        var firstRegion = UUID.randomUUID();
        var secondRegion = UUID.randomUUID();
        Object[] first = {
                firstRegion, "TRAINING", new BigDecimal("0.5"), "SCORE", "HIGH", "MEDIUM",
                "d", "SC-A", "Region A", "Mun A", "SYNTHETIC_DATASET"
        };
        Object[] second = {
                secondRegion, "TRAINING", new BigDecimal("0.9"), "SCORE", "LOW", "HIGH",
                "e", "SC-B", "Region B", "Mun B", "GOVERNMENT"
        };
        when(socialIndicatorJpaRepository.findSocialGapByIndicatorTypePaginated("TRAINING", 20, 0))
                .thenReturn(List.<Object[]>of(first, second));

        List<SocialGapSummary> result = adapter.findByIndicatorType("TRAINING", 20, 0);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).regionId()).isEqualTo(firstRegion);
        assertThat(result.get(1).regionId()).isEqualTo(secondRegion);
        assertThat(result.get(1).sourceType()).isEqualTo("GOVERNMENT");
    }
}
