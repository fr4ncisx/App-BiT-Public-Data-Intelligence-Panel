package com.appbit.geoanalytics.infrastructure.adapter.out.network.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "network_indicators")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NetworkIndicatorEntity {

    @Id
    private UUID id;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "indicator_type", nullable = false, length = 40)
    private String indicatorType;

    @Column(name = "score", nullable = false, precision = 7, scale = 6)
    private BigDecimal score;

    @Column(name = "unit", nullable = false, length = 32)
    private String unit;

    @Column(name = "gap_level", nullable = false, length = 16)
    private String gapLevel;

    @Column(name = "confidence_level", nullable = false, length = 16)
    private String confidenceLevel;

    @Column(name = "period", nullable = false, length = 16)
    private String period;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
