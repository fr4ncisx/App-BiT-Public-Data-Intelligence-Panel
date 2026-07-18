package com.appbit.geoanalytics.application.social.in;

import com.appbit.geoanalytics.application.social.SocialGapResponse;

public interface GetSocialGapUseCase {

    SocialGapResponse execute(String indicatorType, int limit, int offset);
}
