package com.appbit.geoanalytics.application.social.in;

import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorSummary;
import com.appbit.geoanalytics.application.social.SocialGapResponse;
import com.appbit.geoanalytics.application.social.SocialGapResponse.SocialGapItem;
import com.appbit.geoanalytics.application.social.out.SocialGapPort;
import com.appbit.geoanalytics.application.social.out.SocialGapSummary;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetSocialGapService implements GetSocialGapUseCase {

    private static final BigDecimal THRESHOLD_HIGH = new BigDecimal("0.5");
    private static final BigDecimal THRESHOLD_LOW = new BigDecimal("0.5");
    private static final String CONNECTIVITY_TYPE = "CONNECTIVITY_INDEX";

    private final SocialGapPort socialGapPort;
    private final ConcentrationMapPort concentrationMapPort;
    private final NetworkIndicatorMapPort networkIndicatorMapPort;

    @Override
    public SocialGapResponse execute(String indicatorType, int limit, int offset) {
        var summaries = socialGapPort.findByIndicatorType(indicatorType, limit, offset);
        if (summaries.isEmpty()) {
            return new SocialGapResponse(List.of(), List.of());
        }

        var regionIds = summaries.stream().map(SocialGapSummary::regionId).toList();

        var concentrationByRegion = concentrationMapPort.findByRegionIds(regionIds, null).stream()
                .collect(Collectors.toMap(ConcentrationSummary::regionId, Function.identity()));

        var connectivityByRegion = networkIndicatorMapPort.findByRegionIds(regionIds).stream()
                .filter(n -> CONNECTIVITY_TYPE.equals(n.indicatorType()))
                .collect(Collectors.toMap(NetworkIndicatorSummary::regionId, Function.identity()));

        var warnings = new ArrayList<String>();
        var anySeedData = summaries.stream().anyMatch(s -> "SEED_DATA".equals(s.sourceType()));
        if (anySeedData) {
            warnings.add("Los indicadores sociales provienen de datos semilla estimados, no de fuentes oficiales.");
        }

        var items = summaries.stream()
                .map(s -> buildItem(s, indicatorType, concentrationByRegion.get(s.regionId()), connectivityByRegion.get(s.regionId())))
                .sorted(Comparator.comparing(SocialGapItem::priorityLevel,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return new SocialGapResponse(items, List.copyOf(warnings));
    }

    private SocialGapItem buildItem(SocialGapSummary s, String indicatorType,
                                     ConcentrationSummary conc, NetworkIndicatorSummary net) {
        var priority = switch (indicatorType) {
            case "TRAINING" -> computeTrainingPriority(conc, net, s.score());
            case "EMPLOYABILITY" -> computeEmployabilityPriority(conc, s.score());
            case "MENTAL_HEALTH" -> computeMentalHealthPriority(net, s.score());
            case "MENTORSHIP" -> computeMentorshipPriority(s.score(), s.gapLevel());
            default -> computeDefaultPriority(s.gapLevel());
        };
        return new SocialGapItem(s.regionCode(), s.regionName(), s.municipality(),
                s.score(), s.unit(), s.gapLevel(), s.confidenceLevel(),
                s.description(), s.sourceType(), priority);
    }

    private String computeTrainingPriority(ConcentrationSummary conc, NetworkIndicatorSummary net, BigDecimal score) {
        var highConc = conc != null && conc.avgCongestion().compareTo(THRESHOLD_HIGH) >= 0;
        var lowTraining = score.compareTo(THRESHOLD_LOW) < 0;
        var lowConn = net != null && net.score().compareTo(THRESHOLD_LOW) < 0;
        if (highConc && lowTraining && lowConn) return "CRITICAL";
        if (lowTraining && (highConc || lowConn)) return "HIGH";
        if (lowTraining) return "MEDIUM";
        return "LOW";
    }

    private String computeEmployabilityPriority(ConcentrationSummary conc, BigDecimal score) {
        var highConc = conc != null && conc.avgCongestion().compareTo(THRESHOLD_HIGH) >= 0;
        var lowEmploy = score.compareTo(THRESHOLD_LOW) < 0;
        if (highConc && lowEmploy) return "CRITICAL";
        if (lowEmploy) return "HIGH";
        return "MEDIUM";
    }

    private String computeMentalHealthPriority(NetworkIndicatorSummary net, BigDecimal score) {
        var highNeed = score.compareTo(THRESHOLD_LOW) >= 0;
        var lowConn = net != null && net.score().compareTo(THRESHOLD_LOW) < 0;
        if (highNeed && lowConn) return "HIGH";
        if (highNeed) return "MEDIUM";
        return "LOW";
    }

    private String computeMentorshipPriority(BigDecimal score, String gapLevel) {
        var lowCoverage = score.compareTo(THRESHOLD_LOW) < 0;
        var highGap = "HIGH".equals(gapLevel) || "CRITICAL".equals(gapLevel);
        if (lowCoverage && highGap) return "HIGH";
        if (lowCoverage) return "MEDIUM";
        return "LOW";
    }

    private static String computeDefaultPriority(String gapLevel) {
        return switch (gapLevel) {
            case "CRITICAL" -> "HIGH";
            case "HIGH" -> "MEDIUM";
            default -> "LOW";
        };
    }
}
