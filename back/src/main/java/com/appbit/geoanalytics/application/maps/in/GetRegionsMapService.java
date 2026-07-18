package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.IndicatorDetailDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionIndicatorsDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetRegionsMapService implements GetRegionsMapUseCase {

    private final RegionMapPort regionMapPort;
    private final ConcentrationMapPort concentrationMapPort;
    private final SocialIndicatorMapPort socialIndicatorMapPort;

    @Override
    public RegionsMapResponse execute(@Nullable String indicatorType, @Nullable String period, @Nullable String regionCode) {
        List<RegionMapDTO> allRegions = regionMapPort.findAll();

        List<RegionMapDTO> filteredRegions = regionCode != null
                ? allRegions.stream().filter(r -> r.regionCode().equals(regionCode)).toList()
                : allRegions;

        List<UUID> regionIds = filteredRegions.stream().map(RegionMapDTO::id).toList();

        List<ConcentrationSummary> concentrationData = concentrationMapPort.findByRegionIds(regionIds, period);
        List<SocialIndicatorSummary> socialData = socialIndicatorMapPort.findByRegionIds(regionIds, indicatorType);

        Map<UUID, ConcentrationSummary> concentrationByRegion = concentrationData.stream()
                .collect(Collectors.toMap(ConcentrationSummary::regionId, c -> c));

        Map<UUID, List<SocialIndicatorSummary>> socialByRegion = socialData.stream()
                .collect(Collectors.groupingBy(SocialIndicatorSummary::regionId));

        List<RegionMapDTO> resultRegions = filteredRegions.stream()
                .map(region -> buildRegionMapDTO(region, concentrationByRegion, socialByRegion))
                .toList();

        return new RegionsMapResponse(resultRegions);
    }

    private RegionMapDTO buildRegionMapDTO(
            RegionMapDTO region,
            Map<UUID, ConcentrationSummary> concentrationByRegion,
            Map<UUID, List<SocialIndicatorSummary>> socialByRegion
    ) {
        UUID regionId = region.id();

        IndicatorDetailDTO populationConcentration = Optional.ofNullable(concentrationByRegion.get(regionId))
                .map(c -> new IndicatorDetailDTO(
                        BigDecimal.valueOf(c.totalActiveUsers()),
                        "active_users",
                        "tensor_concentracao.csv"))
                .orElse(null);

        IndicatorDetailDTO networkCoverage = Optional.ofNullable(concentrationByRegion.get(regionId))
                .map(c -> new IndicatorDetailDTO(
                        BigDecimal.ONE.subtract(c.avgCongestion()),
                        "index",
                        "tensor_concentracao.csv"))
                .orElse(null);

        IndicatorDetailDTO trainingPrograms = Optional.ofNullable(socialByRegion.get(regionId))
                .flatMap(list -> list.stream().findFirst())
                .map(s -> new IndicatorDetailDTO(
                        s.score(),
                        s.unit(),
                        "social_indicators_seed.csv"))
                .orElse(null);

        return new RegionMapDTO(
                region.id(),
                region.regionCode(),
                region.regionName(),
                region.municipality(),
                region.geoPoint(),
                new RegionIndicatorsDTO(populationConcentration, networkCoverage, trainingPrograms)
        );
    }
}
