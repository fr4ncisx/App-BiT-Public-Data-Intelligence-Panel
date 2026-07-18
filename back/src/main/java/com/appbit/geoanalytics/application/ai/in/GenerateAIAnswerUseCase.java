package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import org.jspecify.annotations.Nullable;

public interface GenerateAIAnswerUseCase {

    AIResponseDTO execute(EvidenceContext evidence, AiIntent intent, @Nullable Language language);
}
