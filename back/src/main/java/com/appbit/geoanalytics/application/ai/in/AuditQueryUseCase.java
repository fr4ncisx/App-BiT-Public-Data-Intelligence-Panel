package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;

public interface AuditQueryUseCase {

    void execute(QueryRequest request, AiIntent intent, AIResponseDTO response, String requestId, String status);
}
