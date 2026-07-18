package com.appbit.geoanalytics.infrastructure.adapter.in.rest.ranking;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;
import com.appbit.geoanalytics.application.ranking.RegionRankingResponse.RankingItem;
import com.appbit.geoanalytics.application.ranking.in.GetRegionRankingUseCase;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegionRankingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetRegionRankingUseCase getRegionRankingUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new RegionRankingController(getRegionRankingUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturnRankingDefaultLimit() throws Exception {
        var item = new RankingItem(1, "CBD_BEIRAMAR", "CBD Beiramar", "Florianopolis",
                new BigDecimal("0.92"), "LOW");
        when(getRegionRankingUseCase.execute(eq("TRAINING"), eq(10)))
                .thenReturn(new RegionRankingResponse(List.of(item)));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/ranking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("indicatorType", "TRAINING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ranking[0].position").value(1))
                .andExpect(jsonPath("$.data.ranking[0].regionCode").value("CBD_BEIRAMAR"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturnRankingWithCustomLimit() throws Exception {
        var item1 = new RankingItem(1, "CBD_BEIRAMAR", "CBD Beiramar", "Florianopolis",
                new BigDecimal("0.92"), "LOW");
        var item2 = new RankingItem(2, "INGLESES", "Ingleses", "Florianopolis",
                new BigDecimal("0.82"), "LOW");
        when(getRegionRankingUseCase.execute(eq("TRAINING"), eq(2)))
                .thenReturn(new RegionRankingResponse(List.of(item1, item2)));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/ranking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("indicatorType", "TRAINING")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ranking.length()").value(2));
    }
}
