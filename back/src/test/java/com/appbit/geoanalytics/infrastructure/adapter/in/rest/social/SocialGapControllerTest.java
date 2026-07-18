package com.appbit.geoanalytics.infrastructure.adapter.in.rest.social;

import com.appbit.geoanalytics.application.social.SocialGapResponse;
import com.appbit.geoanalytics.application.social.SocialGapResponse.SocialGapItem;
import com.appbit.geoanalytics.application.social.in.GetSocialGapUseCase;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SocialGapControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetSocialGapUseCase getSocialGapUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SocialGapController(getSocialGapUseCase, responseFactory, requestContext)
        ).build();
    }

    @Test
    void shouldReturnTrainingGap() throws Exception {
        var item = new SocialGapItem("CBD_BEIRAMAR", "CBD Beiramar", "Florianopolis",
                new BigDecimal("0.75"), "SCORE", "HIGH", "MEDIUM", "Training indicator", "SEED_DATA");
        when(getSocialGapUseCase.execute(eq("TRAINING"), anyInt(), anyInt())).thenReturn(new SocialGapResponse(List.of(item), List.of()));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/social/gap/training").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.indicators[0].regionCode").value("CBD_BEIRAMAR"))
                .andExpect(jsonPath("$.data.indicators[0].gapLevel").value("HIGH"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldReturnEmployabilityGap() throws Exception {
        when(getSocialGapUseCase.execute(eq("EMPLOYABILITY"), anyInt(), anyInt())).thenReturn(new SocialGapResponse(List.of(), List.of()));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/social/gap/employability").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indicators").isEmpty());
    }

    @Test
    void shouldReturnMentalHealthGap() throws Exception {
        when(getSocialGapUseCase.execute(eq("MENTAL_HEALTH"), anyInt(), anyInt())).thenReturn(new SocialGapResponse(List.of(), List.of()));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/social/gap/mental-health").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnMentorshipGap() throws Exception {
        when(getSocialGapUseCase.execute(eq("MENTORSHIP"), anyInt(), anyInt())).thenReturn(new SocialGapResponse(List.of(), List.of()));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/social/gap/mentorship").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnSocialExperienceGap() throws Exception {
        when(getSocialGapUseCase.execute(eq("SOCIAL_EXPERIENCE"), anyInt(), anyInt())).thenReturn(new SocialGapResponse(List.of(), List.of()));
        when(requestContext.requestId()).thenReturn("test-req-id");

        mockMvc.perform(get("/api/v1/social/gap/social-experience").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
