package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class SuggestionsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SuggestedQuestionsService suggestedQuestionsService;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SuggestionsController(suggestedQuestionsService, responseFactory, requestContext)
        ).build();

        when(requestContext.requestId()).thenReturn("test-req-id");
    }

    @Test
    void returnsSuggestions() throws Exception {
        when(suggestedQuestionsService.getSuggestions())
                .thenReturn(List.of("Pregunta 1", "Pregunta 2", "Pregunta 3", "Pregunta 4"));

        mockMvc.perform(get("/api/v1/data/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.DATA_QUERY_PROCESSED.name()))
                .andExpect(jsonPath("$.data[0]").value("Pregunta 1"))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returnsEmptyListWhenNoSuggestionsAvailable() throws Exception {
        when(suggestedQuestionsService.getSuggestions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/data/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
