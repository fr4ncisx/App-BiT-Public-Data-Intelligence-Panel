package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponse;
import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.POPULATION_CONCENTRATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateAIAnswerServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private GenerateAIAnswerService service;

    @Test
    void returnsStructuredResponseWhenEvidenceIsSufficient() {
        var evidence = sufficientEvidence();
        var aiResponse = new AIResponse(
                "Alta concentracion poblacional en la region.",
                "Se observa una concentracion significativa de usuarios activos.",
                "MAP");

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).isEqualTo("Alta concentracion poblacional en la region.");
        assertThat(result.explanation()).contains("concentracion significativa");
        assertThat(result.suggestedVisualization()).isEqualTo("MAP");
        assertThat(result.data()).isNotEmpty();
        assertThat(result.regions()).isNotEmpty();
    }

    @Test
    void returnsInsufficientEvidenceWhenEvidenceIsEmpty() {
        var evidence = EvidenceContext.empty();

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("No hay datos suficientes");
        assertThat(result.data()).isEmpty();
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void returnsFallbackWhenChatClientFails() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("API timeout"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("no esta disponible");
        assertThat(result.data()).isNotEmpty();
        assertThat(result.regions()).isNotEmpty();
        assertThat(result.warnings()).extracting(WarningDTO::type).contains("WARNING");
    }

    @Test
    void returnsFallbackWhenEntityReturnsNull() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(null);

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("no esta disponible");
        assertThat(result.warnings()).extracting(WarningDTO::type).contains("WARNING");
    }

    @Test
    void insufficientEvidenceSkipsAiCall() {
        var evidence = EvidenceContext.empty();

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("No hay datos suficientes");
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().getFirst().type()).isEqualTo("INFO");
    }

    @Test
    void fallsBackToSpanishWhenLanguageIsNull() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("timeout"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, null);

        assertThat(result.summary()).contains("no esta disponible");
    }

    @Test
    void englishResponseWhenLanguageIsEN() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("timeout"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.EN);

        assertThat(result.summary()).contains("currently unavailable");
    }

    @Test
    void portugueseResponseWhenLanguageIsPT() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("timeout"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.PT);

        assertThat(result.summary()).contains("nao esta disponivel");
    }

    @Test
    void responseIncludesEvidenceFromContext() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class)))
                .thenReturn(new AIResponse("OK", "OK", "TABLE"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().indicatorType()).isEqualTo("POPULATION_CONCENTRATION");
        assertThat(result.data().getFirst().value()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result.regions()).hasSize(1);
        assertThat(result.regions().getFirst().regionCode()).isEqualTo("REG_FLORIPA");
        assertThat(result.sources()).containsExactly("Seed Data");
    }

    @Test
    void nullSummaryFallsBackToLocalizedMessage() {
        var evidence = sufficientEvidence();
        var aiResponse = new AIResponse(null, "Explanation", "MAP");

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).isEqualTo("No se pudo generar un resumen.");
        assertThat(result.explanation()).isEqualTo("Explanation");
        assertThat(result.suggestedVisualization()).isEqualTo("MAP");
    }

    @Test
    void nullVisualizationDefaultsToNONE() {
        var evidence = sufficientEvidence();
        var aiResponse = new AIResponse("Summary", "Explanation", null);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.suggestedVisualization()).isEqualTo("NONE");
    }

    private static EvidenceContext sufficientEvidence() {
        return new EvidenceContext(
                List.of(new RegionEvidenceDTO("REG_FLORIPA", "Florianopolis", "Florianopolis",
                        BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6))),
                List.of(new IndicatorEvidenceDTO("POPULATION_CONCENTRATION",
                        BigDecimal.valueOf(5000), "USERS", "Concentracion Poblacional", "HIGH", null)),
                List.of("Seed Data"),
                List.of()
        );
    }
}
