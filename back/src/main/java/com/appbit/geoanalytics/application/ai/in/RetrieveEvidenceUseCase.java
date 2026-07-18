package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import org.jspecify.annotations.Nullable;

public interface RetrieveEvidenceUseCase {

    EvidenceContext execute(AiIntent intent, @Nullable String regionCode,
                            @Nullable String indicatorType, @Nullable String period);
}
