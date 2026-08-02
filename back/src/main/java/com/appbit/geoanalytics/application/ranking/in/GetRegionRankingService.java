package com.appbit.geoanalytics.application.ranking.in;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;
import com.appbit.geoanalytics.application.ranking.RegionRankingResponse.RankingItem;
import com.appbit.geoanalytics.application.social.out.SocialGapPort;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class GetRegionRankingService implements GetRegionRankingUseCase {

    private static final int MAX_RANKING_RESULTS = 1000;

    private final SocialGapPort socialGapPort;

    @Override
    public RegionRankingResponse execute(String indicatorType, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_RANKING_RESULTS));
        var summaries = socialGapPort.findByIndicatorType(indicatorType, safeLimit, 0);
        var position = new AtomicInteger(1);
        var items = summaries.stream()
                .sorted(Comparator.comparing(s -> s.score(), Comparator.reverseOrder()))
                .limit(safeLimit)
                .map(s -> new RankingItem(
                        position.getAndIncrement(),
                        s.regionCode(),
                        s.regionName(),
                        s.municipality(),
                        s.score(),
                        s.gapLevel()))
                .toList();
        return new RegionRankingResponse(items);
    }
}
