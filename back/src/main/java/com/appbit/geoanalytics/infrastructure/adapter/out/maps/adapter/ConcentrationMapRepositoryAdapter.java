package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.ConcentrationSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository.ConcentrationMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConcentrationMapRepositoryAdapter implements ConcentrationMapPort {

    private final ConcentrationMetricJpaRepository concentrationMetricJpaRepository;

    @Override
    public List<ConcentrationSummary> findByRegionIds(List<UUID> regionIds, @Nullable String period) {
        List<Object[]> results = period != null
                ? concentrationMetricJpaRepository.findConcentrationAggregateByRegionIdsAndPeriod(regionIds, period)
                : concentrationMetricJpaRepository.findConcentrationAggregateByRegionIds(regionIds);

        return results.stream()
                .map(row -> new ConcentrationSummary(
                        (UUID) row[0],
                        ((Number) row[1]).longValue(),
                        BigDecimal.valueOf(((Number) row[2]).doubleValue())))
                .toList();
    }
}
