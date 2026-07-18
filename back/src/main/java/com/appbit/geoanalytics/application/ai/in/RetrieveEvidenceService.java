package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.EMPLOYABILITY_GAP;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.MENTAL_HEALTH_ACCESS;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.MENTORSHIP_NEED;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.SOCIAL_EXPERIENCE;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.SOURCE_EXPLANATION;
import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.TRAINING_GAP;

@RequiredArgsConstructor
public class RetrieveEvidenceService implements RetrieveEvidenceUseCase {

    private final RegionMapPort regionMapPort;
    private final ConcentrationMapPort concentrationMapPort;
    private final SocialIndicatorMapPort socialIndicatorMapPort;
    private final NetworkIndicatorMapPort networkIndicatorMapPort;

    @Override
    public EvidenceContext execute(AiIntent intent, @Nullable String regionCode,
                                    @Nullable String indicatorType, @Nullable String period) {
        var regions = regionMapPort.findAll();

        if (regionCode != null) {
            regions = regions.stream()
                    .filter(r -> r.regionCode().equals(regionCode))
                    .toList();
        }

        if (regions.isEmpty()) {
            return EvidenceContext.empty();
        }

        var regionIds = regions.stream().map(RegionMapDTO::id).toList();
        var indicators = new ArrayList<IndicatorEvidenceDTO>();
        var warnings = new ArrayList<WarningDTO>();

        collectConcentration(regionIds, period, indicators);

        if (isSocialRelated(intent)) {
            collectSocialIndicators(regionIds, indicatorType, indicators, warnings);
        } else {
            collectNetworkIndicators(regionIds, indicators, warnings);
        }

        return new EvidenceContext(
                regions.stream().map(this::toRegionEvidence).toList(),
                indicators,
                List.of(),
                warnings
        );
    }

    private void collectConcentration(List<UUID> regionIds, @Nullable String period,
                                       ArrayList<IndicatorEvidenceDTO> indicators) {
        for (var c : concentrationMapPort.findByRegionIds(regionIds, period)) {
            if (c.totalActiveUsers() != null) {
                indicators.add(new IndicatorEvidenceDTO(
                        "POPULATION_CONCENTRATION", BigDecimal.valueOf(c.totalActiveUsers()),
                        "USERS", "Concentración Poblacional", "HIGH", period));
            }
            if (c.avgCongestion() != null) {
                indicators.add(new IndicatorEvidenceDTO(
                        "CONGESTION", c.avgCongestion(),
                        "PERCENT", "Concentración Poblacional", "HIGH", period));
            }
        }
    }

    private void collectSocialIndicators(List<UUID> regionIds, @Nullable String indicatorType,
                                          ArrayList<IndicatorEvidenceDTO> indicators,
                                          ArrayList<WarningDTO> warnings) {
        var socialData = socialIndicatorMapPort.findByRegionIds(regionIds, indicatorType);

        if (socialData.isEmpty()) {
            warnings.add(new WarningDTO("INFO", "No hay indicadores sociales disponibles para los filtros seleccionados."));
            return;
        }

        for (var s : socialData) {
            indicators.add(new IndicatorEvidenceDTO(
                    s.indicatorType(), s.score(),
                    s.unit(), "Indicadores Sociales (Seed)", "LOW", null));
        }

        warnings.add(new WarningDTO("WARNING",
                "Los indicadores sociales provienen de datos semilla estimados, no de fuentes oficiales."));
    }

    private void collectNetworkIndicators(List<UUID> regionIds,
                                           ArrayList<IndicatorEvidenceDTO> indicators,
                                           ArrayList<WarningDTO> warnings) {
        var networkData = networkIndicatorMapPort.findByRegionIds(regionIds);

        if (networkData.isEmpty()) {
            warnings.add(new WarningDTO("INFO",
                    "No hay indicadores de red disponibles para los filtros seleccionados."));
            return;
        }

        for (var n : networkData) {
            indicators.add(new IndicatorEvidenceDTO(
                    n.indicatorType(), n.score(),
                    n.unit(), "Indicadores de Red", "MEDIUM", null));
        }
    }

    private RegionEvidenceDTO toRegionEvidence(RegionMapDTO region) {
        return new RegionEvidenceDTO(
                region.regionCode(),
                region.regionName(),
                region.municipality(),
                region.geoPoint() != null ? region.geoPoint().lat() : BigDecimal.ZERO,
                region.geoPoint() != null ? region.geoPoint().lng() : BigDecimal.ZERO
        );
    }

    private static boolean isSocialRelated(AiIntent intent) {
        return intent == TRAINING_GAP || intent == EMPLOYABILITY_GAP
                || intent == MENTAL_HEALTH_ACCESS || intent == MENTORSHIP_NEED
                || intent == SOCIAL_EXPERIENCE || intent == SOURCE_EXPLANATION;
    }
}
