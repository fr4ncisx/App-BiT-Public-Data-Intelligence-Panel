package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialIndicatorMapRepositoryAdapter implements SocialIndicatorMapPort {

    private final SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @Override
    public List<SocialIndicatorSummary> findByRegionIds(List<UUID> regionIds, @Nullable String indicatorType) {
        List<Object[]> results = indicatorType != null
                ? socialIndicatorJpaRepository.findSocialIndicatorsByRegionIdsAndType(regionIds, indicatorType)
                : socialIndicatorJpaRepository.findSocialIndicatorsByRegionIds(regionIds);

        return results.stream()
                .map(row -> new SocialIndicatorSummary(
                        (UUID) row[0],
                        (String) row[1],
                        (BigDecimal) row[2],
                        (String) row[3]))
                .toList();
    }
}
