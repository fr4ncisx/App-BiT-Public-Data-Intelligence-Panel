package com.appbit.geoanalytics.application.ai.out;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;

import java.util.UUID;

public interface AiAuditPort {

    void saveQueryWithAnswer(UUID id, UUID requestId, QueryRequest request, AiIntent intent,
                             AIResponseDTO response, String status);
}
