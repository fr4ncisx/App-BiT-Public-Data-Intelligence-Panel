package com.appbit.geoanalytics.application.ai;

import java.util.List;

public record EvidenceContext(
        List<RegionEvidenceDTO> regions,
        List<IndicatorEvidenceDTO> indicators,
        List<String> sources,
        List<WarningDTO> warnings
) {
    public static EvidenceContext empty() {
        return new EvidenceContext(List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEvidenceSufficient() {
        if (regions.isEmpty() || indicators.isEmpty()) {
            return false;
        }
        return warnings.stream().noneMatch(w -> "ERROR".equals(w.type()));
    }

    public Confidence confidence() {
        if (regions.isEmpty() || indicators.isEmpty()) {
            return Confidence.INSUFFICIENT;
        }
        if (warnings.stream().anyMatch(w -> "ERROR".equals(w.type()))) {
            return Confidence.INSUFFICIENT;
        }

        var hasWarningWarnings = warnings.stream().anyMatch(w -> "WARNING".equals(w.type()));
        var hasLowConfidenceIndicator = indicators.stream().anyMatch(i -> "LOW".equals(i.confidenceLevel()));
        var isSingleRegion = regions.size() <= 1;
        var isFewIndicators = indicators.size() <= 2;

        if (hasWarningWarnings || hasLowConfidenceIndicator || isSingleRegion || isFewIndicators) {
            return Confidence.MEDIUM;
        }

        return Confidence.HIGH;
    }

    public enum Confidence {
        HIGH, MEDIUM, INSUFFICIENT
    }
}
