package com.appbit.geoanalytics.application.ranking.in;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;
import com.appbit.geoanalytics.application.social.out.SocialGapPort;
import com.appbit.geoanalytics.application.social.out.SocialGapSummary;
import com.appbit.geoanalytics.domain.testing.DomainFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRegionRankingServiceTest {

    @Mock private SocialGapPort socialGapPort;

    private GetRegionRankingService service;

    @BeforeEach
    void setUp() {
        service = new GetRegionRankingService(socialGapPort);
    }

    private SocialGapSummary summary(String code, BigDecimal score) {
        return new SocialGapSummary(DomainFixtures.uuidV7(), code, code, "Florianopolis",
                score, "SCORE", "LOW", "HIGH", "Indicator", "OFFICIAL");
    }

    @Test
    void returnsRankingSortedDescendingAndLimited() {
        when(socialGapPort.findByIndicatorType("TRAINING", 2, 0)).thenReturn(List.of(
                summary("MED", new BigDecimal("0.5")),
                summary("TOP", new BigDecimal("0.9")),
                summary("LOW", new BigDecimal("0.2"))
        ));

        RegionRankingResponse response = service.execute("TRAINING", 2);

        assertThat(response.ranking()).hasSize(2);
        assertThat(response.ranking().get(0).position()).isEqualTo(1);
        assertThat(response.ranking().get(0).regionCode()).isEqualTo("TOP");
        assertThat(response.ranking().get(1).position()).isEqualTo(2);
        assertThat(response.ranking().get(1).regionCode()).isEqualTo("MED");
        assertThat(response.ranking().get(1).score()).isEqualByComparingTo("0.5");
        assertThat(response.ranking().get(1).gapLevel()).isEqualTo("LOW");
    }

    @Test
    void returnsSingleItemWhenLimitIsZeroOrNegative() {
        when(socialGapPort.findByIndicatorType("TRAINING", 1, 0)).thenReturn(List.of(
                summary("A", new BigDecimal("0.9")),
                summary("B", new BigDecimal("0.8"))
        ));

        RegionRankingResponse response = service.execute("TRAINING", 0);

        assertThat(response.ranking()).hasSize(1);
        assertThat(response.ranking().get(0).regionCode()).isEqualTo("A");
    }

    @Test
    void returnsEmptyRankingWhenNoSummaries() {
        when(socialGapPort.findByIndicatorType("TRAINING", 5, 0)).thenReturn(List.of());

        RegionRankingResponse response = service.execute("TRAINING", 5);

        assertThat(response.ranking()).isEmpty();
    }

    @Test
    void requestsSummariesBoundedByRequestedLimit() {
        when(socialGapPort.findByIndicatorType("TRAINING", 5, 0)).thenReturn(List.of());

        service.execute("TRAINING", 5);

        verify(socialGapPort).findByIndicatorType(eq("TRAINING"), eq(5), eq(0));
    }

    @Test
    void capsFetchAtMaxRankingResults() {
        when(socialGapPort.findByIndicatorType("TRAINING", 1000, 0)).thenReturn(List.of(
                summary("A", new BigDecimal("0.9")),
                summary("B", new BigDecimal("0.8"))
        ));

        RegionRankingResponse response = service.execute("TRAINING", 5000);

        assertThat(response.ranking()).hasSize(2);
        verify(socialGapPort).findByIndicatorType(eq("TRAINING"), eq(1000), eq(0));
    }
}
