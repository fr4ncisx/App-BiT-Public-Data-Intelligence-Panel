package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.in.AuditQueryUseCase;
import com.appbit.geoanalytics.application.ai.in.ClassifyIntentUseCase;
import com.appbit.geoanalytics.application.ai.in.GenerateAIAnswerUseCase;
import com.appbit.geoanalytics.application.ai.in.RetrieveEvidenceUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.advice.GlobalExceptionHandler;
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

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.POPULATION_CONCENTRATION;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.UNKNOWN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClassifyIntentUseCase classifyIntentUseCase;

    @Mock
    private RetrieveEvidenceUseCase retrieveEvidenceUseCase;

    @Mock
    private GenerateAIAnswerUseCase generateAIAnswerUseCase;

    @Mock
    private AuditQueryUseCase auditQueryUseCase;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        var exceptionHandler = new GlobalExceptionHandler(responseFactory, requestContext);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new QueryController(classifyIntentUseCase, retrieveEvidenceUseCase,
                        generateAIAnswerUseCase, auditQueryUseCase, responseFactory, requestContext)
        ).setControllerAdvice(exceptionHandler).build();

        when(requestContext.requestId()).thenReturn("test-req-id");
    }

    @Test
    void returns200WithProcessedQuery() throws Exception {
        var region = new RegionEvidenceDTO("REG_FLORIPA", "Florianópolis", "Florianopolis",
                BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6));
        var indicator = new IndicatorEvidenceDTO("POPULATION", BigDecimal.valueOf(100),
                "USERS", "Test", "HIGH", null);
        var evidence = new EvidenceContext(
                List.of(region), List.of(indicator),
                List.of("Seed"), List.of());
        var aiResponse = new AIResponseDTO("Summary", "Explanation", evidence.indicators(),
                evidence.regions(), evidence.sources(), evidence.warnings(), "MAP");

        when(classifyIntentUseCase.execute("concentración poblacional")).thenReturn(POPULATION_CONCENTRATION);
        when(retrieveEvidenceUseCase.execute(eq(POPULATION_CONCENTRATION), any(), any(), any()))
                .thenReturn(evidence);
        when(generateAIAnswerUseCase.execute(eq(evidence), eq(POPULATION_CONCENTRATION), isNull())).thenReturn(aiResponse);
        mockMvc.perform(post("/api/v1/data/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"concentración poblacional\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.DATA_QUERY_PROCESSED.name()))
                .andExpect(jsonPath("$.data.summary").value("Summary"))
                .andExpect(jsonPath("$.data.suggestedVisualization").value("MAP"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void returns400WhenQueryIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/data/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()));
    }

    @Test
    void returns400WhenQueryIsTooLong() throws Exception {
        var longQuery = "a".repeat(501);

        mockMvc.perform(post("/api/v1/data/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"" + longQuery + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()));
    }

    @Test
    void returns200WithInsufficientEvidence() throws Exception {
        var evidence = EvidenceContext.empty();
        var aiResponse = new AIResponseDTO("No hay datos", "Intente con otra región",
                List.of(), List.of(), List.of(), List.of(), "NONE");

        when(classifyIntentUseCase.execute("consulta sin datos")).thenReturn(UNKNOWN);
        when(retrieveEvidenceUseCase.execute(eq(UNKNOWN), any(), any(), any()))
                .thenReturn(evidence);
        when(generateAIAnswerUseCase.execute(eq(evidence), eq(UNKNOWN), isNull())).thenReturn(aiResponse);

        mockMvc.perform(post("/api/v1/data/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"consulta sin datos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.INSUFFICIENT_EVIDENCE.name()))
                .andExpect(jsonPath("$.data.summary").value("No hay datos"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
