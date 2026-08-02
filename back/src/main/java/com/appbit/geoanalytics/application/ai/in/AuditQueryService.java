package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.application.ai.out.AiAuditPort;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AuditQueryService implements AuditQueryUseCase {

    private final AiAuditPort aiAuditPort;

    @Override
    public void execute(QueryRequest request, AiIntent intent, AIResponseDTO response,
                        String requestId, String status) {
        try {
            var queryId = UUID.randomUUID();
            aiAuditPort.saveQueryWithAnswer(queryId, UUID.fromString(requestId), request, intent,
                    response, status);
        } catch (RuntimeException e) {
            log.warn("Failed to persist AI query audit record for requestId={}: {}",
                    requestId, e.getMessage());
        }
    }
}
