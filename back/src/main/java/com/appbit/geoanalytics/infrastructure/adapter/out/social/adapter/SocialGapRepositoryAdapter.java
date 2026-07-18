package com.appbit.geoanalytics.infrastructure.adapter.out.social.adapter;

import com.appbit.geoanalytics.application.social.out.SocialGapPort;
import com.appbit.geoanalytics.application.social.out.SocialGapSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialGapRepositoryAdapter implements SocialGapPort {

    private final SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @Override
    public List<SocialGapSummary> findByIndicatorType(String indicatorType, int limit, int offset) {
        return socialIndicatorJpaRepository.findSocialGapByIndicatorTypePaginated(indicatorType, limit, offset).stream()
                .map(this::toSummary)
                .toList();
    }

    private SocialGapSummary toSummary(Object[] row) {
        return new SocialGapSummary(
                (UUID) row[0],
                (String) row[7],
                (String) row[8],
                (String) row[9],
                (BigDecimal) row[2],
                (String) row[3],
                (String) row[4],
                (String) row[5],
                (String) row[6],
                (String) row[10]
        );
    }
}
