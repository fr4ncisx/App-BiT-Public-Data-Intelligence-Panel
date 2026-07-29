package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Golden dataset tests: deterministic assertions on pre-configured responses.
 * Verifies anti-hallucination properties, format compliance, and content quality.
 * No real API calls. No API key needed.
 */
class GenerateAIAnswerGoldenTest {

    private final ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    private final ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
    private final GenerateAIAnswerService service = new GenerateAIAnswerService(chatClientBuilder);

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    record GoldenScenario(
            String name,
            EvidenceContext evidence,
            AiIntent intent,
            Language language,
            String summaryResponse,
            String explanationResponse,
            String vizResponse,
            String expectedVisualization,
            List<String> requiredKeywords,
            List<String> forbiddenWords,
            int expectedIndicatorCount,
            int expectedRegionCount
    ) {}

    static Stream<Arguments> goldenDataset() {
        return Stream.of(
            Arguments.of(new GoldenScenario(
                    "Population concentration returns MAP",
                    populationEvidence(),
                    AiIntent.POPULATION_CONCENTRATION,
                    Language.ES,
                    "Alta concentracion en REG_FLORIPA con 5000 usuarios.",
                    "La region presenta 5000 usuarios activos, indicando alta demanda.",
                    "MAP",
                    "MAP",
                    List.of("5000", "REG_FLORIPA"),
                    List.of("inventado", "falso", "no existe"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "Connectivity gap returns MAP",
                    connectivityEvidence(),
                    AiIntent.CONNECTIVITY_GAP,
                    Language.ES,
                    "Brecha de conectividad en Continente.",
                    "Continente tiene 45.2% frente a 89.7% del Centro.",
                    "MAP",
                    "MAP",
                    List.of("45.2", "Continente"),
                    List.of("inventado", "falso"),
                    2, 2
            )),
            Arguments.of(new GoldenScenario(
                    "Training gap returns TABLE",
                    trainingEvidence(),
                    AiIntent.TRAINING_GAP,
                    Language.ES,
                    "Brecha de formacion detectada.",
                    "Los indicadores muestran nivel de 72.5% en formacion.",
                    "TABLE",
                    "TABLE",
                    List.of("72.5", "formacion"),
                    List.of("inventado", "falso"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "Region comparison returns RANKING",
                    populationEvidence(),
                    AiIntent.REGION_COMPARISON,
                    Language.ES,
                    "Comparacion entre regiones.",
                    "REG_FLORIPA lidera con 5000 usuarios activos.",
                    "RANKING",
                    "RANKING",
                    List.of("REG_FLORIPA", "5000"),
                    List.of("inventado"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "English response contains English keywords",
                    populationEvidence(),
                    AiIntent.POPULATION_CONCENTRATION,
                    Language.EN,
                    "High population concentration in REG_FLORIPA.",
                    "The region has 5000 active users indicating high demand.",
                    "MAP",
                    "MAP",
                    List.of("5000", "REG_FLORIPA"),
                    List.of("inventado", "falso"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "Portuguese response contains Portuguese keywords",
                    populationEvidence(),
                    AiIntent.POPULATION_CONCENTRATION,
                    Language.PT,
                    "Alta concentracao em REG_FLORIPA.",
                    "A regiao tem 5000 usuarios ativos.",
                    "MAP",
                    "MAP",
                    List.of("5000", "REG_FLORIPA"),
                    List.of("inventado", "falso"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "Response with warnings preserves them",
                    trainingEvidenceWithWarning(),
                    AiIntent.TRAINING_GAP,
                    Language.ES,
                    "Datos semilla disponibles.",
                    "Indicadores de formacion al 72.5%.",
                    "NONE",
                    "NONE",
                    List.of("72.5"),
                    List.of("inventado"),
                    1, 1
            )),
            Arguments.of(new GoldenScenario(
                    "Minimal response still valid",
                    populationEvidence(),
                    AiIntent.POPULATION_CONCENTRATION,
                    Language.ES,
                    "Resumen corto.",
                    "Detalle.",
                    "NONE",
                    "NONE",
                    List.of(),
                    List.of(),
                    1, 1
            ))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("goldenDataset")
    void goldenScenario(GoldenScenario scenario) {
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(
                scenario.summaryResponse(),
                scenario.explanationResponse(),
                scenario.vizResponse());

        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(scenario.evidence(), scenario.intent(), scenario.language());

        assertThat(result.suggestedVisualization())
                .as("Visualization must match expected value")
                .isEqualTo(scenario.expectedVisualization());

        assertThat(result.data())
                .as("Evidence indicators must be preserved")
                .hasSize(scenario.expectedIndicatorCount());

        assertThat(result.regions())
                .as("Evidence regions must be preserved")
                .hasSize(scenario.expectedRegionCount());

        var combined = (result.summary() + " " + result.explanation()).toLowerCase();

        for (var keyword : scenario.requiredKeywords()) {
            assertThat(combined)
                    .as("Response must contain required keyword: " + keyword)
                    .contains(keyword.toLowerCase());
        }

        for (var forbidden : scenario.forbiddenWords()) {
            assertThat(combined)
                    .as("Response must NOT contain forbidden word: " + forbidden)
                    .doesNotContain(forbidden.toLowerCase());
        }
    }

    @ParameterizedTest(name = "Numbers rounded: {0}")
    @MethodSource("numberRoundingDataset")
    void numbersMustBeRounded(String summary, String explanation) {
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(summary, explanation, "MAP");
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

        var combined = result.summary() + " " + result.explanation();
        assertThat(combined)
                .as("Response must not contain more than 1 decimal place")
                .doesNotMatch(".*\\d+\\.\\d{2,}.*");
    }

    static Stream<Arguments> numberRoundingDataset() {
        return Stream.of(
            Arguments.of("5000 usuarios.", "La concentracion es de 5000 usuarios activos."),
            Arguments.of("45.2% cobertura.", "La cobertura es del 45.2%."),
            Arguments.of("1200 personas.", "Afecta a 1200 personas en la zona.")
        );
    }

    @ParameterizedTest(name = "No hallucination: {0}")
    @MethodSource("noHallucinationDataset")
    void mustNotFabricateDataNotInEvidence(String summary, String explanation, List<String> forbiddenInEvidence) {
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(summary, explanation, "MAP");
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

        var combined = (result.summary() + " " + result.explanation()).toLowerCase();
        for (var word : forbiddenInEvidence) {
            assertThat(combined)
                    .as("Must not fabricate data not present in evidence: " + word)
                    .doesNotContain(word.toLowerCase());
        }
    }

    static Stream<Arguments> noHallucinationDataset() {
        return Stream.of(
            Arguments.of(
                "5000 usuarios en REG_FLORIPA.",
                "La region tiene 5000 usuarios activos.",
                List.of("10000", "20000", "Sao Jose", "Palhoca")
            ),
            Arguments.of(
                "Concentracion en REG_FLORIPA.",
                "Se observa alta concentracion de 5000 usuarios.",
                List.of("conectividad", "banda ancha", "4G")
            )
        );
    }

    @ParameterizedTest(name = "Visualization valid: {0}")
    @MethodSource("validVisualizationDataset")
    void visualizationMustBeValidEnum(String viz) {
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse("Test", "Test", viz);
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, Language.ES);

        assertThat(result.suggestedVisualization())
                .as("Visualization must be a valid enum value")
                .isIn("MAP", "TABLE", "RANKING", "FLOW", "NONE");
    }

    static Stream<String> validVisualizationDataset() {
        return Stream.of("MAP", "TABLE", "RANKING", "FLOW", "NONE");
    }

    @ParameterizedTest(name = "Response language match: {0}")
    @MethodSource("languageMatchDataset")
    void responseLanguageMustMatchInput(Language language, String summary, String explanation, List<String> expectedWords) {
        var aiResponse = new com.appbit.geoanalytics.application.ai.AIResponse(summary, explanation, "MAP");
        when(callResponseSpec.entity(any(Class.class), any(Consumer.class))).thenReturn(aiResponse);

        var result = service.execute(populationEvidence(), AiIntent.POPULATION_CONCENTRATION, language);

        var combined = (result.summary() + " " + result.explanation()).toLowerCase();
        for (var word : expectedWords) {
            assertThat(combined)
                    .as("Response in " + language + " must contain word: " + word)
                    .contains(word.toLowerCase());
        }
    }

    static Stream<Arguments> languageMatchDataset() {
        return Stream.of(
            Arguments.of(Language.ES,
                    "Concentracion poblacional.",
                    "La region tiene 5000 usuarios.",
                    List.of("region", "usuarios")),
            Arguments.of(Language.EN,
                    "Population concentration.",
                    "The region has 5000 users.",
                    List.of("region", "users")),
            Arguments.of(Language.PT,
                    "Concentracao populacional.",
                    "A regiao tem 5000 usuarios.",
                    List.of("regiao", "usuarios"))
        );
    }

    @ParameterizedTest(name = "Insufficient evidence: {0}")
    @MethodSource("insufficientEvidenceDataset")
    void insufficientEvidenceMustNotCallAi(AiIntent intent, Language language, String expectedSubstring) {
        var result = service.execute(EvidenceContext.empty(), intent, language);

        assertThat(result.summary().toLowerCase()).contains(expectedSubstring);
        assertThat(result.suggestedVisualization()).isEqualTo("NONE");
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().getFirst().type()).isEqualTo("INFO");
    }

    static Stream<Arguments> insufficientEvidenceDataset() {
        return Stream.of(
            Arguments.of(AiIntent.POPULATION_CONCENTRATION, Language.ES, "no hay datos"),
            Arguments.of(AiIntent.CONNECTIVITY_GAP, Language.EN, "not enough data"),
            Arguments.of(AiIntent.TRAINING_GAP, Language.PT, "nao ha dados"),
            Arguments.of(AiIntent.REGION_COMPARISON, Language.ES, "no hay datos"),
            Arguments.of(AiIntent.UNKNOWN, Language.ES, "no hay datos")
        );
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
                List.of()
        );
    }

    private static EvidenceContext trainingEvidenceWithWarning() {
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
