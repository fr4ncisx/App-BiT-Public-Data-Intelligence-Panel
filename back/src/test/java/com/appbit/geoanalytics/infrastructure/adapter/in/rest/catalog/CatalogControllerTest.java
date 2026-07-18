package com.appbit.geoanalytics.infrastructure.adapter.in.rest.catalog;

import com.appbit.geoanalytics.application.catalog.CatalogResponse;
import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;
import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;
import com.appbit.geoanalytics.application.catalog.in.GetCatalogUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetCatalogUseCase getCatalogUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new CatalogController(getCatalogUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturn200WithCatalog() throws Exception {
        var region = new RegionSummaryDTO("TRINDADE", "Trindade", "Florianopolis");
        var source = new SourceSummaryDTO("Vísent CDRView", "antenas_flp.csv", "SYNTHETIC_DATASET");
        var catalog = new CatalogResponse(
                List.of(region), List.of("TRAINING"), List.of("MADRUGADA"), List.of(source));

        when(getCatalogUseCase.execute()).thenReturn(catalog);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/data/catalog")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.CATALOG_RETRIEVED.name()))
                .andExpect(jsonPath("$.data.regions[0].regionCode").value("TRINDADE"))
                .andExpect(jsonPath("$.data.indicatorTypes[0]").value("TRAINING"))
                .andExpect(jsonPath("$.data.periods[0]").value("MADRUGADA"))
                .andExpect(jsonPath("$.data.sources[0].name").value("Vísent CDRView"))
                .andExpect(jsonPath("$.meta.apiVersion").value("v1"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturn200WithEmptyCatalog() throws Exception {
        var empty = new CatalogResponse(List.of(), List.of(), List.of(), List.of());

        when(getCatalogUseCase.execute()).thenReturn(empty);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/data/catalog")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.regions").isEmpty())
                .andExpect(jsonPath("$.data.indicatorTypes").isEmpty())
                .andExpect(jsonPath("$.data.periods").isEmpty())
                .andExpect(jsonPath("$.data.sources").isEmpty());
    }
}
