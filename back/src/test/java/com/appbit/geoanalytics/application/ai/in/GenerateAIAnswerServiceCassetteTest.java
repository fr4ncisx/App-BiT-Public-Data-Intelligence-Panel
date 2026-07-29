package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateAIAnswerServiceCassetteTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec chatClientRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private GenerateAIAnswerService service;

    @BeforeEach
    void setUp() {
        service = new GenerateAIAnswerService(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void cassettePopulationConcentrationEs() {
        var evidence = populationEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Alta concentracion poblacional en REG_FLORIPA con 5000 usuarios activos.",
                "La region presenta una concentracion significativa de usuarios activos con 5000 personas.",
                "MAP");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).contains("5000");
        assertThat(result.suggestedVisualization()).isEqualTo("MAP");
        assertThat(result.data()).hasSize(1);
        assertThat(result.regions()).hasSize(1);
    }

    @Test
    void cassetteConnectivityGapEs() {
        var evidence = connectivityEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Brecha de conectividad en Continente con 45.2% de cobertura.",
                "Continente tiene 45.2% de cobertura 4G frente al 89.7% del Centro.",
                "MAP");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.CONNECTIVITY_GAP, Language.ES);

        assertThat(result.summary()).contains("45.2");
        assertThat(result.suggestedVisualization()).isEqualTo("MAP");
    }

    @Test
    void cassetteTrainingGapEn() {
        var evidence = trainingEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Training gap detected in Florianopolis sectors.",
                "The data shows training indicators across regions with varying levels.",
                "TABLE");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.TRAINING_GAP, Language.EN);

        assertThat(result.summary()).contains("Training");
        assertThat(result.suggestedVisualization()).isEqualTo("TABLE");
    }

    @Test
    void cassetteRegionComparisonPt() {
        var evidence = populationEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Comparacao entre regioes de Florianopolis.",
                "A regiao REG_FLORIPA apresenta 5000 usuarios ativos.",
                "RANKING");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.REGION_COMPARISON, Language.PT);

        assertThat(result.summary()).contains("Comparacao");
        assertThat(result.suggestedVisualization()).isEqualTo("RANKING");
    }

    @Test
    void cassetteMinimalResponse() {
        var evidence = populationEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Concentracion alta.",
                "5000 usuarios en la zona.",
                "NONE");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.summary()).isEqualTo("Concentracion alta.");
        assertThat(result.explanation()).isEqualTo("5000 usuarios en la zona.");
        assertThat(result.suggestedVisualization()).isEqualTo("NONE");
    }

    @Test
    void cassettePreservesEvidenceInResponse() {
        var evidence = populationEvidence();
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                "Resumen con indicadores.",
                "Detalle con datos.",
                "MAP");

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(evidence, AiIntent.POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().getFirst().indicatorType()).isEqualTo("POPULATION_CONCENTRATION");
        assertThat(result.data().getFirst().value()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result.regions()).hasSize(1);
        assertThat(result.regions().getFirst().regionCode()).isEqualTo("REG_FLORIPA");
        assertThat(result.sources()).containsExactly("Seed Data");
    }

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

    private static EvidenceContext connectivityEvidence() {
        return new EvidenceContext(
                List.of(
                        new RegionEvidenceDTO("REG_CONT", "Continente", "Biguacu",
                                BigDecimal.valueOf(-27.5), BigDecimal.valueOf(-48.6)),
                        new RegionEvidenceDTO("REG_CENTRO", "Centro", "Florianopolis",
                                BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.5))),
                List.of(
                        new IndicatorEvidenceDTO("CONNECTIVITY_GAP",
                                BigDecimal.valueOf(45.2), "PERCENT", "Conectividad", "MEDIUM", null),
                        new IndicatorEvidenceDTO("CONNECTIVITY_GAP",
                                BigDecimal.valueOf(89.7), "PERCENT", "Conectividad", "HIGH", null)),
                List.of("Seed Data"),
                List.of()
        );
    }

    private static EvidenceContext trainingEvidence() {
        return new EvidenceContext(
                List.of(new RegionEvidenceDTO("REG_FLORIPA", "Florianopolis", "Florianopolis",
                        BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6))),
                List.of(new IndicatorEvidenceDTO("TRAINING_GAP",
                        BigDecimal.valueOf(72.5), "PERCENT", "Formacion", "MEDIUM", null)),
                List.of("Seed Data"),
                List.of(new WarningDTO("WARNING", "Datos semilla estimados."))
        );
    }
}
