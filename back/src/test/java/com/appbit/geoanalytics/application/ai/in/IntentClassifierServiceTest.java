package com.appbit.geoanalytics.application.ai.in;

import org.junit.jupiter.api.Test;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.*;
import static org.assertj.core.api.Assertions.assertThat;

class IntentClassifierServiceTest {

    private final IntentClassifierService service = new IntentClassifierService();

    @Test
    void detectsTrainingGapSpanish() {
        assertThat(service.execute("brecha de formación en floripa")).isEqualTo(TRAINING_GAP);
    }

    @Test
    void detectsTrainingGapPortuguese() {
        assertThat(service.execute("lacuna de formação na trindade")).isEqualTo(TRAINING_GAP);
    }

    @Test
    void detectsEmployabilityGapSpanish() {
        assertThat(service.execute("baja empleabilidad en la región")).isEqualTo(EMPLOYABILITY_GAP);
    }

    @Test
    void detectsEmployabilityGapPortuguese() {
        assertThat(service.execute("desemprego em floripa")).isEqualTo(EMPLOYABILITY_GAP);
    }

    @Test
    void detectsMentalHealthAccess() {
        assertThat(service.execute("saúde mental na comunidade")).isEqualTo(MENTAL_HEALTH_ACCESS);
    }

    @Test
    void detectsConnectivityGap() {
        assertThat(service.execute("conectividad 4g en la region")).isEqualTo(CONNECTIVITY_GAP);
    }

    @Test
    void detectsPopulationConcentration() {
        assertThat(service.execute("concentración de habitantes")).isEqualTo(POPULATION_CONCENTRATION);
    }

    @Test
    void detectsRegionComparison() {
        assertThat(service.execute("comparar região trindade com capoeiras")).isEqualTo(REGION_COMPARISON);
    }

    @Test
    void detectsSourceExplanation() {
        assertThat(service.execute("de onde vêm esses dados")).isEqualTo(SOURCE_EXPLANATION);
    }

    @Test
    void returnsUnknownForUnrelatedQuery() {
        assertThat(service.execute("olá como você está")).isEqualTo(UNKNOWN);
    }

    @Test
    void returnsUnknownForNull() {
        assertThat(service.execute(null)).isEqualTo(UNKNOWN);
    }

    @Test
    void returnsUnknownForBlank() {
        assertThat(service.execute("   ")).isEqualTo(UNKNOWN);
    }
}
