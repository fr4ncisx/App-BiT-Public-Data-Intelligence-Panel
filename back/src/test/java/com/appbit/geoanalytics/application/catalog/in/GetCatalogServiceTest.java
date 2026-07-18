package com.appbit.geoanalytics.application.catalog.in;

import com.appbit.geoanalytics.application.catalog.CatalogResponse;
import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;
import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;
import com.appbit.geoanalytics.application.catalog.out.IndicatorTypeCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.PeriodCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.RegionCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.SourceCatalogPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetCatalogServiceTest {

    private final RegionCatalogPort regionCatalogPort = mock(RegionCatalogPort.class);
    private final SourceCatalogPort sourceCatalogPort = mock(SourceCatalogPort.class);
    private final PeriodCatalogPort periodCatalogPort = mock(PeriodCatalogPort.class);
    private final IndicatorTypeCatalogPort indicatorTypeCatalogPort = mock(IndicatorTypeCatalogPort.class);
    private final GetCatalogService service = new GetCatalogService(
            regionCatalogPort, sourceCatalogPort, periodCatalogPort, indicatorTypeCatalogPort);

    @Test
    void shouldReturnCompleteCatalog() {
        var region = new RegionSummaryDTO("TRINDADE", "Trindade", "Florianopolis");
        var source = new SourceSummaryDTO("Vísent CDRView", "antenas_flp.csv", "SYNTHETIC_DATASET");

        when(regionCatalogPort.findAll()).thenReturn(List.of(region));
        when(sourceCatalogPort.findAll()).thenReturn(List.of(source));
        when(periodCatalogPort.findAll()).thenReturn(List.of("MADRUGADA", "MANHA"));
        when(indicatorTypeCatalogPort.findAll()).thenReturn(List.of("TRAINING", "EMPLOYABILITY"));

        CatalogResponse result = service.execute();

        assertThat(result.regions()).containsExactly(region);
        assertThat(result.sources()).containsExactly(source);
        assertThat(result.periods()).containsExactly("MADRUGADA", "MANHA");
        assertThat(result.indicatorTypes()).containsExactly("TRAINING", "EMPLOYABILITY");
    }

    @Test
    void shouldReturnEmptyListsWhenNoData() {
        when(regionCatalogPort.findAll()).thenReturn(List.of());
        when(sourceCatalogPort.findAll()).thenReturn(List.of());
        when(periodCatalogPort.findAll()).thenReturn(List.of());
        when(indicatorTypeCatalogPort.findAll()).thenReturn(List.of());

        CatalogResponse result = service.execute();

        assertThat(result.regions()).isEmpty();
        assertThat(result.sources()).isEmpty();
        assertThat(result.periods()).isEmpty();
        assertThat(result.indicatorTypes()).isEmpty();
    }

    @Test
    void shouldCallAllPorts() {
        when(regionCatalogPort.findAll()).thenReturn(List.of());
        when(sourceCatalogPort.findAll()).thenReturn(List.of());
        when(periodCatalogPort.findAll()).thenReturn(List.of());
        when(indicatorTypeCatalogPort.findAll()).thenReturn(List.of());

        service.execute();

        verify(regionCatalogPort).findAll();
        verify(sourceCatalogPort).findAll();
        verify(periodCatalogPort).findAll();
        verify(indicatorTypeCatalogPort).findAll();
    }
}
