package com.appbit.geoanalytics.application.social.out;

import java.util.List;

public interface SocialGapPort {

    List<SocialGapSummary> findByIndicatorType(String indicatorType, int limit, int offset);
}
