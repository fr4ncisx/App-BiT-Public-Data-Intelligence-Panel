package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
class GenerateAIAnswerServiceLiveTest {

    private static EvidenceContext populationEvidence() {
        return new EvidenceContext(
                List.of(new RegionEvidenceDTO("REG_FLORIPA", "Florianopolis", "Florianopolis",
                        BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6))),
                List.of(new IndicatorEvidenceDTO("POPULATION_CONCENTRATION",
                        BigDecimal.valueOf(5000), "USERS", "Concentracion Poblacional", "HIGH", null)),
                List.of("Seed Data"),
                List.of()
        );
    }

    private static boolean isGeminiAvailable(String apiKey) {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("test-key");
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test-ai")
    class DevProfileTest {

        @Autowired
        private GenerateAIAnswerService service;

        @Value("${spring.ai.google.genai.api-key:}")
        private String apiKey;

        @Test
        void returnsValidResponseEs() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            assertThat(result.summary()).isNotBlank();
            assertThat(result.explanation()).isNotBlank();
            assertThat(result.suggestedVisualization()).isIn("MAP", "TABLE", "RANKING", "FLOW", "NONE");
        }

        @Test
        void returnsValidResponseEn() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.EN);

            assertThat(result.summary()).isNotBlank();
            assertThat(result.explanation()).isNotBlank();
            assertThat(result.suggestedVisualization()).isIn("MAP", "TABLE", "RANKING", "FLOW", "NONE");
        }

        @Test
        void returnsValidResponsePt() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.PT);

            assertThat(result.summary()).isNotBlank();
            assertThat(result.explanation()).isNotBlank();
            assertThat(result.suggestedVisualization()).isIn("MAP", "TABLE", "RANKING", "FLOW", "NONE");
        }

        @Test
        void returnsCannedResponseWithInsufficientEvidence() {
            var result = service.execute(EvidenceContext.empty(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            assertThat(result.summary()).contains("No hay datos suficientes");
            assertThat(result.suggestedVisualization()).isEqualTo("NONE");
            assertThat(result.warnings()).hasSize(1);
        }

        @Test
        void doesNotFabricateDataNotInEvidence() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            var combined = (result.summary() + " " + result.explanation()).toLowerCase();
            assertThat(combined).doesNotContain("sao jose", "palhoca", "10000", "20000");
        }

        @Test
        void numbersAreRounded() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            var combined = result.summary() + " " + result.explanation();
            assertThat(combined).doesNotMatch(".*\\d+\\.\\d{2,}.*");
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test-prod")
    class ProdProfileTest {

        @Autowired
        private GenerateAIAnswerService service;

        @Value("${spring.ai.google.genai.api-key:}")
        private String apiKey;

        @Test
        void returnsValidResponseWithProdModel() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            assertThat(result.summary()).isNotBlank();
            assertThat(result.explanation()).isNotBlank();
            assertThat(result.suggestedVisualization()).isIn("MAP", "TABLE", "RANKING", "FLOW", "NONE");
        }

        @Test
        void doesNotFabricateDataWithProdModel() {
            Assumptions.assumeTrue(isGeminiAvailable(apiKey), "Gemini API key not configured");

            var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

            var combined = (result.summary() + " " + result.explanation()).toLowerCase();
            assertThat(combined).doesNotContain("sao jose", "palhoca", "10000", "20000");
        }
    }
}
