package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorSummary;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.CONNECTIVITY_GAP;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.SOURCE_EXPLANATION;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.TRAINING_GAP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrieveEvidenceServiceTest {

    @Mock
    private RegionMapPort regionMapPort;

    @Mock
    private ConcentrationMapPort concentrationMapPort;

    @Mock
    private SocialIndicatorMapPort socialIndicatorMapPort;

    @Mock
    private NetworkIndicatorMapPort networkIndicatorMapPort;

    @InjectMocks
    private RetrieveEvidenceService service;

    @Test
    void returnsEmptyWhenNoRegions() {
        when(regionMapPort.findAll()).thenReturn(List.of());

        var result = service.execute(TRAINING_GAP, null, null, null);

        assertThat(result.regions()).isEmpty();
        assertThat(result.indicators()).isEmpty();
        assertThat(result.isEvidenceSufficient()).isFalse();
    }

    @Test
    void returnsEmptyWhenRegionCodeDoesNotMatch() {
        var region = region("REG_FLORIPA", "Florianópolis", "Florianopolis");
        when(regionMapPort.findAll()).thenReturn(List.of(region));

        var result = service.execute(TRAINING_GAP, "REG_NONEXISTENT", null, null);

        assertThat(result.regions()).isEmpty();
        assertThat(result.isEvidenceSufficient()).isFalse();
    }

    @Test
    void retrievesSocialEvidenceForTrainingGap() {
        var region = region("REG_TRINDADE", "Trindade", "Florianopolis");
        var regionId = region.id();
        when(regionMapPort.findAll()).thenReturn(List.of(region));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of(new ConcentrationSummary(regionId, 5000L, BigDecimal.valueOf(45.2))));
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of(new SocialIndicatorSummary(regionId, "TRAINING", BigDecimal.valueOf(0.3), "SCORE")));

        var result = service.execute(TRAINING_GAP, null, null, null);

        assertThat(result.regions()).hasSize(1);
        assertThat(result.regions().getFirst().regionCode()).isEqualTo("REG_TRINDADE");
        assertThat(result.indicators()).hasSize(3);
        assertThat(result.isEvidenceSufficient()).isTrue();
        assertThat(result.warnings()).isNotEmpty();

        verifyNoInteractions(networkIndicatorMapPort);
    }

    @Test
    void warnsWhenSocialDataIsEmpty() {
        var region = region("REG_FLORIPA", "Florianópolis", "Florianopolis");
        var regionId = region.id();
        when(regionMapPort.findAll()).thenReturn(List.of(region));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of(new ConcentrationSummary(regionId, 5000L, BigDecimal.valueOf(45.2))));
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of());

        var result = service.execute(TRAINING_GAP, null, null, null);

        assertThat(result.regions()).hasSize(1);
        assertThat(result.indicators()).hasSize(2);
        assertThat(result.warnings()).extracting(com.appbit.geoanalytics.application.ai.WarningDTO::message)
                .anyMatch(msg -> msg.contains("No hay indicadores sociales disponibles"));
    }

    @Test
    void warnsWhenNetworkDataIsEmpty() {
        var region = region("REG_FLORIPA", "Florianópolis", "Florianopolis");
        var regionId = region.id();
        when(regionMapPort.findAll()).thenReturn(List.of(region));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(List.of(regionId)))
                .thenReturn(List.of());

        var result = service.execute(CONNECTIVITY_GAP, null, null, null);

        assertThat(result.regions()).hasSize(1);
        assertThat(result.indicators()).isEmpty();
        assertThat(result.warnings()).extracting(com.appbit.geoanalytics.application.ai.WarningDTO::message)
                .anyMatch(msg -> msg.contains("No hay indicadores de red disponibles"));
    }

    @Test
    void retrievesNetworkEvidenceForConnectivityGap() {
        var region = region("REG_TRINDADE", "Trindade", "Florianopolis");
        var regionId = region.id();
        when(regionMapPort.findAll()).thenReturn(List.of(region));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of());
        when(networkIndicatorMapPort.findByRegionIds(List.of(regionId)))
                .thenReturn(List.of(new NetworkIndicatorSummary(regionId, "COVERAGE_INDEX",
                        BigDecimal.valueOf(0.85), "SCORE")));

        var result = service.execute(CONNECTIVITY_GAP, null, null, null);

        assertThat(result.regions()).hasSize(1);
        assertThat(result.indicators()).hasSize(1);
        assertThat(result.indicators().getFirst().indicatorType()).isEqualTo("COVERAGE_INDEX");
        assertThat(result.isEvidenceSufficient()).isTrue();

        verifyNoInteractions(socialIndicatorMapPort);
    }

    @Test
    void filtersByRegionCode() {
        var region1 = region("REG_FLORIPA", "Florianópolis", "Florianopolis");
        var region2 = region("REG_TRINDADE", "Trindade", "Florianopolis");
        when(regionMapPort.findAll()).thenReturn(List.of(region1, region2));

        var result = service.execute(SOURCE_EXPLANATION, "REG_FLORIPA", null, null);

        assertThat(result.regions()).hasSize(1);
        assertThat(result.regions().getFirst().regionCode()).isEqualTo("REG_FLORIPA");
    }

    private static RegionMapDTO region(String code, String name, String municipality) {
        return new RegionMapDTO(UUID.randomUUID(), code, name, municipality,
                new GeoPointDTO(BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6)), null);
    }
}
