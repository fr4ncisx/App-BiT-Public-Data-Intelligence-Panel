package com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiAnswerEntity {

    @Id
    private UUID id;

    @Column(name = "query_id", nullable = false)
    private UUID queryId;

    @Column(name = "summary", nullable = false, length = 1000)
    private String summary;

    @Column(name = "explanation", nullable = false, length = 4000)
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false)
    private String data;

    @Column(name = "suggested_visualization", nullable = false, length = 16)
    private String suggestedVisualization;

    @Column(name = "confidence_level", nullable = false, length = 16)
    private String confidenceLevel;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "ai_answer_evidence", joinColumns = @JoinColumn(name = "answer_id"))
    @OrderColumn(name = "position")
    @Column(name = "evidence_text", nullable = false, length = 1000)
    private List<String> evidence;

    @ElementCollection
    @CollectionTable(name = "ai_answer_regions", joinColumns = @JoinColumn(name = "answer_id"))
    @OrderColumn(name = "position")
    @Column(name = "region_id", nullable = false)
    private List<UUID> regionIds;

    @ElementCollection
    @CollectionTable(name = "ai_answer_sources", joinColumns = @JoinColumn(name = "answer_id"))
    @OrderColumn(name = "position")
    @Column(name = "source_id", nullable = false)
    private List<UUID> sourceIds;

    @ElementCollection
    @CollectionTable(name = "ai_answer_warnings", joinColumns = @JoinColumn(name = "answer_id"))
    @OrderColumn(name = "position")
    @Column(name = "warning_text", nullable = false, length = 500)
    private List<String> warnings;
}
