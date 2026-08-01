package com.appbit.geoanalytics.application.social.in;

import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorSummary;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSocialGapServiceTest {

    @Mock private SocialGapPort socialGapPort;
    @Mock private ConcentrationMapPort concentrationMapPort;
    @Mock private NetworkIndicatorMapPort networkIndicatorMapPort;

    private GetSocialGapService service;

    @BeforeEach
    void setUp() {
        service = new GetSocialGapService(socialGapPort, concentrationMapPort, networkIndicatorMapPort);
    }

    private SocialGapSummary summary(UUID regionId, String code, BigDecimal score, String gapLevel, String sourceType) {
        return new SocialGapSummary(regionId, code, code, "Florianopolis", score, "SCORE",
                gapLevel, "HIGH", "Indicator", sourceType);
    }

    private ConcentrationSummary concentration(UUID regionId, BigDecimal avgCongestion) {
        return new ConcentrationSummary(regionId, 1000L, avgCongestion);
    }

    private NetworkIndicatorSummary connectivity(UUID regionId, BigDecimal score) {
        return new NetworkIndicatorSummary(regionId, "CONNECTIVITY_INDEX", score, "SCORE");
    }

    @Test
    void returnsEmptyResponseWhenNoSummaries() {
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(List.of());

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.indicators()).isEmpty();
        assertThat(response.warnings()).isEmpty();
        verifyNoInteractions(concentrationMapPort, networkIndicatorMapPort);
    }

    @Test
    void returnsTrainingItemsSortedByPriority() {
        var criticalRegion = DomainFixtures.uuidV7();
        var lowRegion = DomainFixtures.uuidV7();
        var summaries = List.of(
                summary(lowRegion, "LOW_REG", new BigDecimal("0.8"), "LOW", "OFFICIAL"),
                summary(criticalRegion, "CRIT_REG", new BigDecimal("0.2"), "HIGH", "OFFICIAL")
        );
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(summaries);
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of(
                concentration(criticalRegion, new BigDecimal("0.9")),
                concentration(lowRegion, new BigDecimal("0.1"))
        ));
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of(
                connectivity(criticalRegion, new BigDecimal("0.2")),
                new NetworkIndicatorSummary(lowRegion, "SIGNAL_QUALITY", new BigDecimal("0.9"), "SCORE")
        ));

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.indicators()).extracting("regionCode").containsExactly("CRIT_REG", "LOW_REG");
        assertThat(response.indicators()).extracting("priorityLevel").containsExactly("CRITICAL", "LOW");
        assertThat(response.warnings()).isEmpty();
    }

    @Test
    void computesTrainingHighWithHighCongestionOnly() {
        var region = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(
                List.of(summary(region, "REG_A", new BigDecimal("0.2"), "LOW", "OFFICIAL")));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(
                List.of(concentration(region, new BigDecimal("0.9"))));
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.indicators().get(0).priorityLevel()).isEqualTo("HIGH");
    }

    @Test
    void computesTrainingHighWithLowConnectivityOnly() {
        var region = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(
                List.of(summary(region, "REG_A", new BigDecimal("0.2"), "LOW", "OFFICIAL")));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(
                List.of(connectivity(region, new BigDecimal("0.2"))));

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.indicators().get(0).priorityLevel()).isEqualTo("HIGH");
    }

    @Test
    void computesTrainingMediumWhenOnlyLowScore() {
        var region = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(
                List.of(summary(region, "REG_A", new BigDecimal("0.2"), "LOW", "OFFICIAL")));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.indicators().get(0).priorityLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void computesEmployabilityPriorities() {
        var critical = DomainFixtures.uuidV7();
        var high = DomainFixtures.uuidV7();
        var medium = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("EMPLOYABILITY", 10, 0)).thenReturn(List.of(
                summary(critical, "CRIT", new BigDecimal("0.2"), "HIGH", "OFFICIAL"),
                summary(high, "HIGH", new BigDecimal("0.2"), "HIGH", "OFFICIAL"),
                summary(medium, "MED", new BigDecimal("0.8"), "LOW", "OFFICIAL")
        ));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of(
                concentration(critical, new BigDecimal("0.9")),
                concentration(high, new BigDecimal("0.1")),
                concentration(medium, new BigDecimal("0.1"))
        ));
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var priorities = service.execute("EMPLOYABILITY", 10, 0).indicators()
                .stream().map(s -> s.priorityLevel()).toList();

        assertThat(priorities).contains("CRITICAL", "HIGH", "MEDIUM");
    }

    @Test
    void computesMentalHealthPriorities() {
        var high = DomainFixtures.uuidV7();
        var medium = DomainFixtures.uuidV7();
        var low = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("MENTAL_HEALTH", 10, 0)).thenReturn(List.of(
                summary(high, "HIGH", new BigDecimal("0.8"), "HIGH", "OFFICIAL"),
                summary(medium, "MED", new BigDecimal("0.8"), "LOW", "OFFICIAL"),
                summary(low, "LOW", new BigDecimal("0.2"), "LOW", "OFFICIAL")
        ));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of(
                connectivity(high, new BigDecimal("0.2"))
        ));

        var priorities = service.execute("MENTAL_HEALTH", 10, 0).indicators()
                .stream().map(s -> s.priorityLevel()).toList();

        assertThat(priorities).containsExactlyInAnyOrder("HIGH", "MEDIUM", "LOW");
    }

    @Test
    void computesMentorshipPriorities() {
        var high = DomainFixtures.uuidV7();
        var medium = DomainFixtures.uuidV7();
        var low = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("MENTORSHIP", 10, 0)).thenReturn(List.of(
                summary(high, "HIGH", new BigDecimal("0.2"), "CRITICAL", "OFFICIAL"),
                summary(medium, "MED", new BigDecimal("0.2"), "LOW", "OFFICIAL"),
                summary(low, "LOW", new BigDecimal("0.8"), "CRITICAL", "OFFICIAL")
        ));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var priorities = service.execute("MENTORSHIP", 10, 0).indicators()
                .stream().map(s -> s.priorityLevel()).toList();

        assertThat(priorities).containsExactlyInAnyOrder("HIGH", "MEDIUM", "LOW");
    }

    @Test
    void computesDefaultPrioritiesForUnknownIndicatorType() {
        var high = DomainFixtures.uuidV7();
        var medium = DomainFixtures.uuidV7();
        var low = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("OTHER", 10, 0)).thenReturn(List.of(
                summary(high, "HIGH", new BigDecimal("0.5"), "CRITICAL", "OFFICIAL"),
                summary(medium, "MED", new BigDecimal("0.5"), "HIGH", "OFFICIAL"),
                summary(low, "LOW", new BigDecimal("0.5"), "UNKNOWN", "OFFICIAL")
        ));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var priorities = service.execute("OTHER", 10, 0).indicators()
                .stream().map(s -> s.priorityLevel()).toList();

        assertThat(priorities).containsExactlyInAnyOrder("HIGH", "MEDIUM", "LOW");
    }

    @Test
    void addsSeedDataWarningWhenAnySummaryUsesSeedData() {
        var region = DomainFixtures.uuidV7();
        when(socialGapPort.findByIndicatorType("TRAINING", 10, 0)).thenReturn(
                List.of(summary(region, "REG_A", new BigDecimal("0.8"), "LOW", "SEED_DATA")));
        when(concentrationMapPort.findByRegionIds(any(), isNull())).thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(any())).thenReturn(List.of());

        var response = service.execute("TRAINING", 10, 0);

        assertThat(response.warnings())
                .containsExactly("Los indicadores sociales provienen de datos semilla estimados, no de fuentes oficiales.");
    }

    @Test
    void passesLimitAndOffsetToPort() {
        when(socialGapPort.findByIndicatorType("TRAINING", 25, 5)).thenReturn(List.of());

        service.execute("TRAINING", 25, 5);

        verify(socialGapPort).findByIndicatorType(eq("TRAINING"), eq(25), eq(5));
    }
}
