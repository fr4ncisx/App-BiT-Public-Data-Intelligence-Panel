package com.appbit.geoanalytics.infrastructure.adapter.in.rest.sources;

import com.appbit.geoanalytics.application.sources.SourceDTO;
import com.appbit.geoanalytics.application.sources.SourcesResponse;
import com.appbit.geoanalytics.application.sources.in.GetSourcesUseCase;
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
class SourcesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetSourcesUseCase getSourcesUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SourcesController(getSourcesUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturn200WithSources() throws Exception {
        var source = new SourceDTO(
                "V\u00edsent CDRView - Antennas", "antenas_flp.csv", "SYNTHETIC_DATASET",
                "Antenas y coordenadas", null, null, null, null, "COMPLETED",
                Instant.parse("2026-07-04T00:00:00Z"),
                Instant.parse("2026-07-04T00:01:00Z"),
                132L, 132L, 0L, "");

        var response = new SourcesResponse(List.of(source));

        when(getSourcesUseCase.execute()).thenReturn(response);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/data/sources")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.SOURCES_RETRIEVED.name()))
                .andExpect(jsonPath("$.data.sources[0].sourceName").value("V\u00edsent CDRView - Antennas"))
                .andExpect(jsonPath("$.data.sources[0].fileName").value("antenas_flp.csv"))
                .andExpect(jsonPath("$.data.sources[0].sourceType").value("SYNTHETIC_DATASET"))
                .andExpect(jsonPath("$.data.sources[0].lastIngestionState").value("COMPLETED"))
                .andExpect(jsonPath("$.data.sources[0].rowsRead").value(132))
                .andExpect(jsonPath("$.data.sources[0].rowsInserted").value(132))
                .andExpect(jsonPath("$.data.sources[0].rowsRejected").value(0))
                .andExpect(jsonPath("$.meta.apiVersion").value("v1"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturn200WithEmptySources() throws Exception {
        var empty = new SourcesResponse(List.of());

        when(getSourcesUseCase.execute()).thenReturn(empty);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/data/sources")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sources").isEmpty());
    }
}
