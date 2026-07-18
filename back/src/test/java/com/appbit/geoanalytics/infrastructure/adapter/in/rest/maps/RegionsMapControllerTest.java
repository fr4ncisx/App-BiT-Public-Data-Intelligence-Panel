package com.appbit.geoanalytics.infrastructure.adapter.in.rest.maps;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.in.GetRegionsMapUseCase;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegionsMapControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetRegionsMapUseCase getRegionsMapUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new RegionsMapController(getRegionsMapUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturn200WithRegions() throws Exception {
        var region = new RegionMapDTO(
                UUID.randomUUID(), "CBD_BEIRAMAR", "CBD Beiramar", "Florianopolis",
                new GeoPointDTO(new BigDecimal("-27.5954"), new BigDecimal("-48.5480")),
                null);
        var response = new RegionsMapResponse(List.of(region));

        when(getRegionsMapUseCase.execute(null, null, null)).thenReturn(response);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/maps/regions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.MAP_REGIONS_RETRIEVED.name()))
                .andExpect(jsonPath("$.data.regions[0].regionCode").value("CBD_BEIRAMAR"))
                .andExpect(jsonPath("$.data.regions[0].geoPoint.lat").value(-27.5954))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldPassQueryParams() throws Exception {
        var response = new RegionsMapResponse(List.of());

        when(getRegionsMapUseCase.execute(eq("TRAINING"), eq("MANHA"), eq("CBD_BEIRAMAR")))
                .thenReturn(response);
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/maps/regions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("indicatorType", "TRAINING")
                        .param("period", "MANHA")
                        .param("regionCode", "CBD_BEIRAMAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.regions").isEmpty());
    }
}
