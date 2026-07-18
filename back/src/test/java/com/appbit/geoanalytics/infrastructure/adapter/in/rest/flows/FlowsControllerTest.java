package com.appbit.geoanalytics.infrastructure.adapter.in.rest.flows;

import com.appbit.geoanalytics.application.maps.FlowsResponse;
import com.appbit.geoanalytics.application.maps.FlowsResponse.FlowDTO;
import com.appbit.geoanalytics.application.maps.FlowsResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.in.GetFlowsUseCase;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlowsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetFlowsUseCase getFlowsUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FlowsController(getFlowsUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturnFlows() throws Exception {
        var flow = new FlowDTO(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "CBD Beiramar", "Centro", "Florianopolis", "Florianopolis",
                new GeoPointDTO(new BigDecimal("-27.5954"), new BigDecimal("-48.5480")),
                new GeoPointDTO(new BigDecimal("-27.5968"), new BigDecimal("-48.5492")),
                false, 12500L, 18750L, new BigDecimal("5.3"), "MANHA");
        var response = new FlowsResponse(List.of(flow));

        when(getFlowsUseCase.execute()).thenReturn(response);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/maps/flows").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flows[0].originClusterName").value("CBD Beiramar"))
                .andExpect(jsonPath("$.data.flows[0].usersCount").value(12500))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
