package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetRegionsMapServiceTest {

    private final RegionMapPort regionMapPort = mock(RegionMapPort.class);
    private final ConcentrationMapPort concentrationMapPort = mock(ConcentrationMapPort.class);
    private final SocialIndicatorMapPort socialIndicatorMapPort = mock(SocialIndicatorMapPort.class);
    private final GetRegionsMapService service = new GetRegionsMapService(
            regionMapPort, concentrationMapPort, socialIndicatorMapPort);

    private final UUID regionId = UUID.randomUUID();

    private final RegionMapDTO regionDTO = new RegionMapDTO(
            regionId, "CBD_BEIRAMAR", "CBD Beiramar", "Florianopolis",
            new GeoPointDTO(new BigDecimal("-27.5954"), new BigDecimal("-48.5480")),
            null
    );

    @Test
    void shouldReturnAllRegionsWithIndicators() {
        when(regionMapPort.findAll()).thenReturn(List.of(regionDTO));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of(new ConcentrationSummary(regionId, 12450L, new BigDecimal("0.26"))));
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), null))
                .thenReturn(List.of(new SocialIndicatorSummary(regionId, "TRAINING", new BigDecimal("3"), "programs")));

        RegionsMapResponse response = service.execute(null, null, null);

        assertThat(response.regions()).hasSize(1);
        RegionMapDTO result = response.regions().getFirst();
        assertThat(result.id()).isEqualTo(regionId);
        assertThat(result.regionCode()).isEqualTo("CBD_BEIRAMAR");
        assertThat(result.geoPoint().lat()).isEqualByComparingTo("-27.5954");

        assertThat(result.indicators().populationConcentration()).isNotNull();
        assertThat(result.indicators().populationConcentration().value()).isEqualByComparingTo("12450");
        assertThat(result.indicators().populationConcentration().unit()).isEqualTo("active_users");

        assertThat(result.indicators().networkCoverage()).isNotNull();
        assertThat(result.indicators().networkCoverage().value()).isEqualByComparingTo("0.74");
        assertThat(result.indicators().networkCoverage().unit()).isEqualTo("index");

        assertThat(result.indicators().trainingPrograms()).isNotNull();
        assertThat(result.indicators().trainingPrograms().value()).isEqualByComparingTo("3");
        assertThat(result.indicators().trainingPrograms().unit()).isEqualTo("programs");
    }

    @Test
    void shouldReturnNullIndicatorsWhenNoData() {
        when(regionMapPort.findAll()).thenReturn(List.of(regionDTO));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null)).thenReturn(List.of());
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), null)).thenReturn(List.of());

        RegionsMapResponse response = service.execute(null, null, null);

        assertThat(response.regions()).hasSize(1);
        RegionMapDTO result = response.regions().getFirst();
        assertThat(result.indicators().populationConcentration()).isNull();
        assertThat(result.indicators().networkCoverage()).isNull();
        assertThat(result.indicators().trainingPrograms()).isNull();
    }

    @Test
    void shouldFilterByRegionCode() {
        var otherId = UUID.randomUUID();
        var otherRegion = new RegionMapDTO(
                otherId, "OUTRA", "Outra Região", "Florianopolis",
                new GeoPointDTO(BigDecimal.ZERO, BigDecimal.ZERO), null);

        when(regionMapPort.findAll()).thenReturn(List.of(regionDTO, otherRegion));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), null)).thenReturn(List.of());
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), null)).thenReturn(List.of());

        RegionsMapResponse response = service.execute(null, null, "CBD_BEIRAMAR");

        assertThat(response.regions()).hasSize(1);
        assertThat(response.regions().getFirst().regionCode()).isEqualTo("CBD_BEIRAMAR");
    }

    @Test
    void shouldPassPeriodAndIndicatorTypeToPorts() {
        when(regionMapPort.findAll()).thenReturn(List.of(regionDTO));
        when(concentrationMapPort.findByRegionIds(List.of(regionId), "MANHA")).thenReturn(List.of());
        when(socialIndicatorMapPort.findByRegionIds(List.of(regionId), "TRAINING")).thenReturn(List.of());

        service.execute("TRAINING", "MANHA", null);

        verify(concentrationMapPort).findByRegionIds(List.of(regionId), "MANHA");
        verify(socialIndicatorMapPort).findByRegionIds(List.of(regionId), "TRAINING");
    }
}
