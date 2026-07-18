package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.application.maps.CompareRegionsResponse;
import com.appbit.geoanalytics.application.maps.CompareRegionsResponse.IndicatorDetailDTO;
import com.appbit.geoanalytics.application.maps.CompareRegionsResponse.RegionCompareDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorSummary;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CompareRegionsService implements CompareRegionsUseCase {

    private final RegionMapPort regionMapPort;
    private final ConcentrationMapPort concentrationMapPort;
    private final SocialIndicatorMapPort socialIndicatorMapPort;
    private final NetworkIndicatorMapPort networkIndicatorMapPort;

    @Override
    public CompareRegionsResponse execute(String regionCodeA, String regionCodeB) {
        List<RegionsMapResponse.RegionMapDTO> allRegions = regionMapPort.findAll();

        var regionA = allRegions.stream()
                .filter(r -> r.regionCode().equals(regionCodeA))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionCodeA));

        var regionB = allRegions.stream()
                .filter(r -> r.regionCode().equals(regionCodeB))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + regionCodeB));

        List<UUID> regionIds = List.of(regionA.id(), regionB.id());

        List<ConcentrationSummary> concentrationData = concentrationMapPort.findByRegionIds(regionIds, null);
        List<SocialIndicatorSummary> socialData = socialIndicatorMapPort.findByRegionIds(regionIds, null);
        List<NetworkIndicatorSummary> networkData = networkIndicatorMapPort.findByRegionIds(regionIds);

        Map<UUID, ConcentrationSummary> concentrationByRegion = concentrationData.stream()
                .collect(Collectors.toMap(ConcentrationSummary::regionId, c -> c));

        Map<UUID, List<SocialIndicatorSummary>> socialByRegion = socialData.stream()
                .collect(Collectors.groupingBy(SocialIndicatorSummary::regionId));

        Map<UUID, List<NetworkIndicatorSummary>> networkByRegion = networkData.stream()
                .collect(Collectors.groupingBy(NetworkIndicatorSummary::regionId));

        RegionCompareDTO dtoA = buildRegionCompare(regionA, concentrationByRegion, socialByRegion, networkByRegion);
        RegionCompareDTO dtoB = buildRegionCompare(regionB, concentrationByRegion, socialByRegion, networkByRegion);

        List<WarningDTO> warnings = new ArrayList<>();
        if (concentrationData.isEmpty()) {
            warnings.add(new WarningDTO("INFO", "No hay datos de concentración para las regiones seleccionadas."));
        }
        if (socialData.isEmpty()) {
            warnings.add(new WarningDTO("INFO", "No hay indicadores sociales para las regiones seleccionadas."));
        }
        if (networkData.isEmpty()) {
            warnings.add(new WarningDTO("INFO", "No hay indicadores de red para las regiones seleccionadas."));
        }

        return new CompareRegionsResponse(dtoA, dtoB, warnings);
    }

    private RegionCompareDTO buildRegionCompare(
            RegionsMapResponse.RegionMapDTO region,
            Map<UUID, ConcentrationSummary> concentrationByRegion,
            Map<UUID, List<SocialIndicatorSummary>> socialByRegion,
            Map<UUID, List<NetworkIndicatorSummary>> networkByRegion
    ) {
        UUID regionId = region.id();

        IndicatorDetailDTO concentration = Optional.ofNullable(concentrationByRegion.get(regionId))
                .map(c -> new IndicatorDetailDTO(
                        "POPULATION_CONCENTRATION",
                        BigDecimal.valueOf(c.totalActiveUsers()),
                        "active_users",
                        "tensor_concentracao.csv"))
                .orElse(null);

        IndicatorDetailDTO networkCoverage = Optional.ofNullable(networkByRegion.get(regionId))
                .flatMap(list -> list.stream().findFirst())
                .map(n -> new IndicatorDetailDTO(
                        "NETWORK_COVERAGE",
                        n.score(),
                        n.unit(),
                        "network_indicators"))
                .orElse(null);

        List<IndicatorDetailDTO> socialIndicators = socialByRegion.getOrDefault(regionId, List.of()).stream()
                .map(s -> new IndicatorDetailDTO(
                        s.indicatorType(),
                        s.score(),
                        s.unit(),
                        "social_indicators_seed.csv"))
                .toList();

        return new RegionCompareDTO(
                region.regionCode(),
                region.regionName(),
                region.municipality(),
                concentration,
                networkCoverage,
                socialIndicators
        );
    }
}
