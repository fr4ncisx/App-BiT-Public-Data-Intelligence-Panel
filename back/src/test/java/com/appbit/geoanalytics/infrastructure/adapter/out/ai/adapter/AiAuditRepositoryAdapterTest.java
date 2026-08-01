package com.appbit.geoanalytics.infrastructure.adapter.out.ai.adapter;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.testing.DomainFixtures;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiAnswerEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiQueryEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository.AiAnswerJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository.AiQueryJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.POPULATION_CONCENTRATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiAuditRepositoryAdapterTest {

    @Mock private AiQueryJpaRepository aiQueryJpaRepository;
    @Mock private AiAnswerJpaRepository aiAnswerJpaRepository;
    @Mock private RegionJpaRepository regionJpaRepository;
    @Mock private DataSourceJpaRepository dataSourceJpaRepository;
    @Mock private IdGeneratorPort idGeneratorPort;

    private AiAuditRepositoryAdapter adapter;

    private static final UUID QUERY_ID = DomainFixtures.uuidV7();
    private static final UUID REQUEST_ID = DomainFixtures.uuidV7();
    private static final UUID ANSWER_ID = DomainFixtures.uuidV7();
    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private static final UUID REGION_ID = DomainFixtures.uuidV7();

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(ANSWER_ID);
        adapter = new AiAuditRepositoryAdapter(
                aiQueryJpaRepository, aiAnswerJpaRepository, regionJpaRepository,
                dataSourceJpaRepository, idGeneratorPort
        );
    }

    private AIResponseDTO response(
            List<IndicatorEvidenceDTO> data,
            List<RegionEvidenceDTO> regions,
            List<String> sources,
            List<WarningDTO> warnings,
            String visualization
    ) {
        return new AIResponseDTO(
                "Summary", "Explanation", data, regions, sources, warnings, visualization
        );
    }

    private IndicatorEvidenceDTO indicator(String sourceName, String confidence) {
        return new IndicatorEvidenceDTO("POPULATION", BigDecimal.valueOf(5000), "USERS",
                sourceName, confidence, "MANHA");
    }

    private RegionEvidenceDTO region(String code) {
        return new RegionEvidenceDTO(code, "Florianópolis", "Florianopolis",
                BigDecimal.valueOf(-27.6), BigDecimal.valueOf(-48.6));
    }

    private void stubSource(String sourceName, String fileName) {
        when(dataSourceJpaRepository.findAll()).thenReturn(List.of(
                DataSourceEntity.builder().id(SOURCE_ID).sourceName(sourceName).fileName(fileName).build()
        ));
    }

    private AiQueryEntity capturedQuery() {
        var captor = ArgumentCaptor.forClass(AiQueryEntity.class);
        verify(aiQueryJpaRepository).save(captor.capture());
        return captor.getValue();
    }

    private AiAnswerEntity capturedAnswer() {
        var captor = ArgumentCaptor.forClass(AiAnswerEntity.class);
        verify(aiAnswerJpaRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void persistsQueryAndAnswerWithHighConfidence() {
        stubSource("Vísent CDRView - Concentration", "tensor_concentracao.csv");
        when(regionJpaRepository.findByRegionCode("REG_FLORIPA")).thenReturn(
                Optional.of(RegionEntity.builder().id(REGION_ID).build()));

        var request = new QueryRequest("concentración en floripa", "REG_FLORIPA", "POPULATION", "MANHA", null);
        var aiResponse = response(
                List.of(indicator("Concentración Poblacional", "HIGH")),
                List.of(region("REG_FLORIPA")),
                List.of("Concentración Poblacional"),
                List.of(new WarningDTO("INFO", "Seed data")),
                "MAP"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        var query = capturedQuery();
        assertThat(query.getId()).isEqualTo(QUERY_ID);
        assertThat(query.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(query.getQueryText()).isEqualTo("concentración en floripa");
        assertThat(query.getLanguage()).isEqualTo("ES");
        assertThat(query.getIntent()).isEqualTo(AiIntent.POPULATION_CONCENTRATION.name());
        assertThat(query.getStatus()).isEqualTo("PROCESSED");
        assertThat(query.getFilters())
                .isEqualTo("{\"regionCode\":\"REG_FLORIPA\",\"indicatorType\":\"POPULATION\",\"period\":\"MANHA\"}");

        var answer = capturedAnswer();
        assertThat(answer.getId()).isEqualTo(ANSWER_ID);
        assertThat(answer.getQueryId()).isEqualTo(QUERY_ID);
        assertThat(answer.getSummary()).isEqualTo("Summary");
        assertThat(answer.getExplanation()).isEqualTo("Explanation");
        assertThat(answer.getSuggestedVisualization()).isEqualTo("MAP");
        assertThat(answer.getConfidenceLevel()).isEqualTo("HIGH");
        assertThat(answer.getEvidence()).containsExactly("POPULATION: 5000 USERS (Concentración Poblacional)");
        assertThat(answer.getRegionIds()).containsExactly(REGION_ID);
        assertThat(answer.getSourceIds()).containsExactly(SOURCE_ID);
        assertThat(answer.getWarnings()).containsExactly("Seed data");
        assertThat(answer.getData()).contains("POPULATION").contains("5000");
    }

    @Test
    void resolvesSourceIdByFileNameWhenSourceNameDiffers() {
        stubSource("Custom Name", "Vísent CDRView - Antennas");

        var request = new QueryRequest("red", null, null, null, null);
        var aiResponse = response(
                List.of(),
                List.of(),
                List.of("Indicadores de Red"),
                List.of(),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getSourceIds()).containsExactly(SOURCE_ID);
    }

    @Test
    void resolvesSourceIdsFromDataEvidenceWhenSourcesEmpty() {
        stubSource("Social Indicators Seed", "seed.csv");

        var request = new QueryRequest("social", null, null, null, null);
        var aiResponse = response(
                List.of(
                        indicator("Indicadores Sociales (Seed)", "MEDIUM"),
                        indicator("Indicadores Sociales (Seed)", "MEDIUM")
                ),
                List.of(),
                List.of(),
                List.of(),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        var answer = capturedAnswer();
        assertThat(answer.getSourceIds()).containsExactly(SOURCE_ID);
        assertThat(answer.getData()).contains("Indicadores Sociales (Seed)");
    }

    @Test
    void leavesSourceIdsEmptyAndUsesUnknownInEvidenceWhenNothingResolvable() {
        when(dataSourceJpaRepository.findAll()).thenReturn(List.of());

        var request = new QueryRequest("sin datos", null, null, null, null);
        var aiResponse = response(
                List.of(indicator(null, "LOW")),
                List.of(),
                List.of("Fuente desconocida"),
                List.of(),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        var answer = capturedAnswer();
        assertThat(answer.getSourceIds()).isEmpty();
        assertThat(answer.getEvidence()).containsExactly("POPULATION: 5000 USERS (Unknown)");
    }

    @Test
    void filtersOutUnresolvedRegionIds() {
        stubSource("Some Source", "some.csv");
        var resolved = DomainFixtures.uuidV7();
        when(regionJpaRepository.findByRegionCode("REG_A")).thenReturn(
                Optional.of(RegionEntity.builder().id(resolved).build()));
        when(regionJpaRepository.findByRegionCode("REG_B")).thenReturn(Optional.empty());

        var request = new QueryRequest("regiones", null, null, null, null);
        var aiResponse = response(
                List.of(),
                List.of(region("REG_A"), region("REG_B")),
                List.of("Some Source"),
                List.of(),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getRegionIds()).containsExactly(resolved);
    }

    @Test
    void resolvesConfidenceLowWhenRegionsOrDataAreMissing() {
        stubSource("Some Source", "some.csv");

        var request = new QueryRequest("vacío", null, null, null, null);
        var aiResponse = response(List.of(), List.of(), List.of("Some Source"), List.of(), "NONE");

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getConfidenceLevel()).isEqualTo("LOW");
    }

    @Test
    void resolvesConfidenceMediumWhenWarningPresent() {
        stubSource("Some Source", "some.csv");
        when(regionJpaRepository.findByRegionCode("REG_FLORIPA")).thenReturn(
                Optional.of(RegionEntity.builder().id(REGION_ID).build()));

        var request = new QueryRequest("alerta", null, null, null, null);
        var aiResponse = response(
                List.of(indicator("Some Source", "HIGH")),
                List.of(region("REG_FLORIPA")),
                List.of("Some Source"),
                List.of(new WarningDTO("WARNING", "Datos estimados")),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getConfidenceLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void resolvesConfidenceMediumWhenIndicatorHasLowConfidence() {
        stubSource("Some Source", "some.csv");
        when(regionJpaRepository.findByRegionCode("REG_FLORIPA")).thenReturn(
                Optional.of(RegionEntity.builder().id(REGION_ID).build()));

        var request = new QueryRequest("baja confianza", null, null, null, null);
        var aiResponse = response(
                List.of(indicator("Some Source", "LOW")),
                List.of(region("REG_FLORIPA")),
                List.of("Some Source"),
                List.of(),
                "NONE"
        );

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getConfidenceLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void uppercasesRequestLanguage() {
        stubSource("Some Source", "some.csv");

        var request = new QueryRequest("idioma", null, null, null, "pt");
        var aiResponse = response(List.of(), List.of(), List.of("Some Source"), List.of(), "NONE");

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedQuery().getLanguage()).isEqualTo("PT");
    }

    @Test
    void escapesQuotesInFilterValues() {
        stubSource("Some Source", "some.csv");

        var request = new QueryRequest("filtros", "RE\"G", "POP\"", "\"", null);
        var aiResponse = response(List.of(), List.of(), List.of("Some Source"), List.of(), "NONE");

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedQuery().getFilters())
                .isEqualTo("{\"regionCode\":\"RE\\\"G\",\"indicatorType\":\"POP\\\"\",\"period\":\"\\\"\"}");
    }

    @Test
    void defaultsVisualizationToNoneWhenNull() {
        stubSource("Some Source", "some.csv");

        var request = new QueryRequest("vis", null, null, null, null);
        var aiResponse = response(List.of(), List.of(), List.of("Some Source"), List.of(), null);

        adapter.saveQueryWithAnswer(QUERY_ID, REQUEST_ID, request, POPULATION_CONCENTRATION, aiResponse, "PROCESSED");

        assertThat(capturedAnswer().getSuggestedVisualization()).isEqualTo("NONE");
    }
}
