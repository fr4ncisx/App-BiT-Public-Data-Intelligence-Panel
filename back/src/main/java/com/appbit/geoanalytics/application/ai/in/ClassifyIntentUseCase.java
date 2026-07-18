package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.domain.ai.enums.AiIntent;

public interface ClassifyIntentUseCase {

    AiIntent execute(String query);
}
