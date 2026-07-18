package com.appbit.geoanalytics.application.ai.in;

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
    void returnsResponseWhenEvidenceIsSufficient() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("""
                SUMMARY: Alta concentración poblacional en la región.
                EXPLANATION: Se observa una concentración significativa de usuarios activos.
                SUGGESTED_VISUALIZATION: MAP
                """);

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).isEqualTo("Alta concentración poblacional en la región.");
        assertThat(result.explanation()).contains("concentración significativa");
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
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("API timeout"));

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("no está disponible");
        assertThat(result.data()).isNotEmpty();
        assertThat(result.regions()).isNotEmpty();
        assertThat(result.warnings()).extracting(WarningDTO::type).contains("WARNING");
    }

    @Test
    void parsesResponseWithDefaultValuesWhenFieldsMissing() {
        var evidence = sufficientEvidence();

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Some random response without fields");

        var result = service.execute(evidence, POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).isEqualTo("No se pudo generar un resumen.");
        assertThat(result.explanation()).isEqualTo("No se pudo generar una explicación.");
        assertThat(result.suggestedVisualization()).isEqualTo("NONE");
    }

    private static EvidenceContext sufficientEvidence() {
        return new EvidenceContext(
                List.of(new RegionEvidenceDTO("REG_FLORIPA", "Florianópolis", "Florianopolis",
                        BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6))),
                List.of(new IndicatorEvidenceDTO("POPULATION_CONCENTRATION",
                        BigDecimal.valueOf(5000), "USERS", "Concentración Poblacional", "HIGH", null)),
                List.of("Seed Data"),
                List.of()
        );
    }
}
