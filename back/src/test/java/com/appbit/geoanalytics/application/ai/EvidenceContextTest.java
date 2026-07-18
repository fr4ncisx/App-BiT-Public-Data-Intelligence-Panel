package com.appbit.geoanalytics.application.ai;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvidenceContextTest {

    @Test
    void emptyReturnsInsufficient() {
        var context = EvidenceContext.empty();

        assertThat(context.isEvidenceSufficient()).isFalse();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.INSUFFICIENT);
    }

    @Test
    void sufficientWithoutWarnings() {
        var context = new EvidenceContext(
                List.of(region("REG_A"), region("REG_B"), region("REG_C")),
                List.of(indicator("POPULATION", "HIGH"), indicator("CONGESTION", "HIGH"), indicator("COVERAGE", "HIGH")),
                List.of("source"),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.HIGH);
    }

    @Test
    void sufficientWithInfoWarnings() {
        var context = new EvidenceContext(
                List.of(region("REG_A"), region("REG_B"), region("REG_C")),
                List.of(indicator("POPULATION", "HIGH"), indicator("CONGESTION", "HIGH"), indicator("COVERAGE", "HIGH")),
                List.of("source"),
                List.of(new WarningDTO("INFO", "Some info"))
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.HIGH);
    }

    @Test
    void insufficientWithEmptyRegions() {
        var context = new EvidenceContext(
                List.of(),
                List.of(indicator("POPULATION", "HIGH")),
                List.of(),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isFalse();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.INSUFFICIENT);
    }

    @Test
    void insufficientWithEmptyIndicators() {
        var context = new EvidenceContext(
                List.of(region("REG_A")),
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isFalse();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.INSUFFICIENT);
    }

    @Test
    void insufficientWithErrorWarning() {
        var context = new EvidenceContext(
                List.of(region("REG_A")),
                List.of(indicator("POPULATION", "HIGH")),
                List.of("source"),
                List.of(new WarningDTO("ERROR", "Critical data issue"))
        );

        assertThat(context.isEvidenceSufficient()).isFalse();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.INSUFFICIENT);
    }

    @Test
    void mediumConfidenceWithWarningWarning() {
        var context = new EvidenceContext(
                List.of(region("REG_A")),
                List.of(indicator("POPULATION", "HIGH")),
                List.of("source"),
                List.of(new WarningDTO("WARNING", "Seed data used"))
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.MEDIUM);
    }

    @Test
    void mediumConfidenceWithSingleRegion() {
        var context = new EvidenceContext(
                List.of(region("REG_A")),
                List.of(indicator("POPULATION", "HIGH"), indicator("CONGESTION", "HIGH")),
                List.of("source"),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.MEDIUM);
    }

    @Test
    void mediumConfidenceWithFewIndicators() {
        var context = new EvidenceContext(
                List.of(region("REG_A"), region("REG_B")),
                List.of(indicator("POPULATION", "HIGH")),
                List.of("source"),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.MEDIUM);
    }

    @Test
    void mediumConfidenceWithLowReliabilityIndicator() {
        var context = new EvidenceContext(
                List.of(region("REG_A"), region("REG_B")),
                List.of(indicator("POPULATION", "LOW"), indicator("CONGESTION", "HIGH")),
                List.of("source"),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.MEDIUM);
    }

    @Test
    void highConfidenceRequiresMultipleRegionsAndIndicators() {
        var context = new EvidenceContext(
                List.of(region("REG_A"), region("REG_B"), region("REG_C")),
                List.of(indicator("POPULATION", "HIGH"), indicator("CONGESTION", "HIGH"), indicator("COVERAGE", "HIGH")),
                List.of("source"),
                List.of()
        );

        assertThat(context.isEvidenceSufficient()).isTrue();
        assertThat(context.confidence()).isEqualTo(EvidenceContext.Confidence.HIGH);
    }

    private static RegionEvidenceDTO region(String code) {
        return new RegionEvidenceDTO(code, "Region " + code, "Municipality",
                BigDecimal.valueOf(-27.0), BigDecimal.valueOf(-48.0));
    }

    private static IndicatorEvidenceDTO indicator(String type, String confidence) {
        return new IndicatorEvidenceDTO(type, BigDecimal.valueOf(100), "UNIT", "Source", confidence, null);
    }
}