package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.application.ai.out.AiAuditPort;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class AuditQueryService implements AuditQueryUseCase {

    private final AiAuditPort aiAuditPort;

    @Override
    public void execute(QueryRequest request, AiIntent intent, AIResponseDTO response,
                        String requestId, String status) {
        var queryId = UUID.randomUUID();
        aiAuditPort.saveQueryWithAnswer(queryId, UUID.fromString(requestId), request, intent,
                response, status);
    }
}
