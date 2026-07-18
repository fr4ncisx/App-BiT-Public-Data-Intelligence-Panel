package com.appbit.geoanalytics.application.ranking.in;

import com.appbit.geoanalytics.application.ranking.RegionRankingResponse;

public interface GetRegionRankingUseCase {

    RegionRankingResponse execute(String indicatorType, int limit);
}
