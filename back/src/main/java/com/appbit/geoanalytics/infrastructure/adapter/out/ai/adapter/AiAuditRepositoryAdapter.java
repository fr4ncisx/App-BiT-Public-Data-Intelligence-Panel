package com.appbit.geoanalytics.infrastructure.adapter.out.ai.adapter;

import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.QueryRequest;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.out.AiAuditPort;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiAnswerEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiQueryEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository.AiQueryJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository.AiAnswerJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AiAuditRepositoryAdapter implements AiAuditPort {

    private final AiQueryJpaRepository aiQueryJpaRepository;
    private final AiAnswerJpaRepository aiAnswerJpaRepository;
    private final RegionJpaRepository regionJpaRepository;
    private final DataSourceJpaRepository dataSourceJpaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IdGeneratorPort idGeneratorPort;

    @Override
    @Transactional
    public void saveQueryWithAnswer(UUID id, UUID requestId, QueryRequest request, AiIntent intent,
                                    AIResponseDTO response, String status) {
        var query = buildQueryEntity(id, requestId, request, intent, status);
        aiQueryJpaRepository.save(query);

        var answer = buildAnswerEntity(id, response);
        aiAnswerJpaRepository.save(answer);
    }

    private AiQueryEntity buildQueryEntity(UUID id, UUID requestId, QueryRequest request,
                                            AiIntent intent, String status) {
        return AiQueryEntity.builder()
                .id(id)
                .requestId(requestId)
                .queryText(request.query())
                .language(request.language() != null ? request.language().toUpperCase() : "ES")
                .intent(intent.name())
                .filters(buildFiltersJson(request))
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    private AiAnswerEntity buildAnswerEntity(UUID queryId, AIResponseDTO response) {
        var allSources = dataSourceJpaRepository.findAll();

        var sourceIds = response.sources().stream()
                .map(name -> resolveSourceId(name, allSources))
                .filter(Objects::nonNull)
                .toList();

        if (sourceIds.isEmpty() && response.data() != null) {
            sourceIds = response.data().stream()
                    .map(IndicatorEvidenceDTO::sourceName)
                    .filter(Objects::nonNull)
                    .map(name -> resolveSourceId(name, allSources))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        var regionIds = response.regions().stream()
                .map(RegionEvidenceDTO::regionCode)
                .map(regionJpaRepository::findByRegionCode)
                .filter(java.util.Optional::isPresent)
                .map(opt -> opt.get().getId())
                .toList();

        var warnings = response.warnings().stream()
                .map(WarningDTO::message)
                .toList();

        var evidence = response.data().stream()
                .map(i -> "%s: %s %s (%s)".formatted(
                        i.indicatorType(),
                        i.value(),
                        i.unit(),
                        i.sourceName() != null ? i.sourceName() : "Unknown"
                ))
                .toList();

        return AiAnswerEntity.builder()
                .id(idGeneratorPort.generate())
                .queryId(queryId)
                .summary(response.summary())
                .explanation(response.explanation())
                .data(serializeData(response.data()))
                .suggestedVisualization(response.suggestedVisualization() != null ? response.suggestedVisualization() : "NONE")
                .confidenceLevel(resolveConfidence(response))
                .createdAt(Instant.now())
                .evidence(evidence)
                .regionIds(regionIds)
                .sourceIds(sourceIds)
                .warnings(warnings)
                .build();
    }

    private UUID resolveSourceId(String name, List<DataSourceEntity> allSources) {
        String dbName = switch (name) {
            case "Concentración Poblacional" -> "Vísent CDRView - Concentration";
            case "Indicadores de Red" -> "Vísent CDRView - Antennas";
            case "Indicadores Sociales (Seed)" -> "Social Indicators Seed";
            default -> name;
        };
        return allSources.stream()
                .filter(src -> src.getSourceName().equalsIgnoreCase(dbName) || src.getFileName().equalsIgnoreCase(dbName))
                .map(DataSourceEntity::getId)
                .findFirst()
                .orElse(null);
    }

    private static String resolveConfidence(AIResponseDTO response) {
        if (response.regions().isEmpty() || response.data().isEmpty()) {
            return "LOW";
        }
        boolean hasWarnings = response.warnings().stream()
                .anyMatch(w -> "WARNING".equalsIgnoreCase(w.type()));
        boolean hasLowConfidence = response.data().stream()
                .anyMatch(i -> "LOW".equalsIgnoreCase(i.confidenceLevel()));

        if (hasWarnings || hasLowConfidence) {
            return "MEDIUM";
        }
        return "HIGH";
    }

    private String serializeData(List<IndicatorEvidenceDTO> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static String buildFiltersJson(QueryRequest request) {
        return "{\"regionCode\":\"%s\",\"indicatorType\":\"%s\",\"period\":\"%s\"}"
                .formatted(
                        safeJson(request.regionCode()),
                        safeJson(request.indicatorType()),
                        safeJson(request.period())
                );
    }

    private static String safeJson(String value) {
        return value != null ? value.replace("\"", "\\\"") : "";
    }
}
