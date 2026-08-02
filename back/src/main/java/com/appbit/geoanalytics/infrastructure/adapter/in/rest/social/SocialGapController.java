package com.appbit.geoanalytics.infrastructure.adapter.in.rest.social;

import com.appbit.geoanalytics.application.social.SocialGapResponse;
import com.appbit.geoanalytics.application.social.in.GetSocialGapUseCase;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class SocialGapController implements SocialGapApi {

    private final GetSocialGapUseCase getSocialGapUseCase;
    private final ApiResponseFactory responseFactory;
    private final RequestContext requestContext;

    @Override
    public ResponseEntity<ApiResponse<SocialGapResponse>> getTrainingGap(int limit, int offset) {
        return execute("TRAINING", limit, offset);
    }

    @Override
    public ResponseEntity<ApiResponse<SocialGapResponse>> getEmployabilityGap(int limit, int offset) {
        return execute("EMPLOYABILITY", limit, offset);
    }

    @Override
    public ResponseEntity<ApiResponse<SocialGapResponse>> getMentalHealthGap(int limit, int offset) {
        return execute("MENTAL_HEALTH", limit, offset);
    }

    @Override
    public ResponseEntity<ApiResponse<SocialGapResponse>> getMentorshipGap(int limit, int offset) {
        return execute("MENTORSHIP", limit, offset);
    }

    @Override
    public ResponseEntity<ApiResponse<SocialGapResponse>> getSocialExperienceGap(int limit, int offset) {
        return execute("SOCIAL_EXPERIENCE", limit, offset);
    }

    private ResponseEntity<ApiResponse<SocialGapResponse>> execute(String indicatorType, int limit, int offset) {
        SocialGapResponse data = getSocialGapUseCase.execute(indicatorType, limit, offset);
        return responseFactory.success(
                HttpStatus.OK,
                ApiResponseCode.SOCIAL_GAP_RETRIEVED,
                "Social gap indicators retrieved successfully",
                data,
                requestContext.requestId()
        );
    }
}
