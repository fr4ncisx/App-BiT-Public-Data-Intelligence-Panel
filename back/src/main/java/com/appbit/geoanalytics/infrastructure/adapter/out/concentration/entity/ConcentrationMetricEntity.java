package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.entity;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "concentration_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConcentrationMetricEntity {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "ecgi", nullable = false, length = 16)
    private String ecgi;

    @Column(name = "cluster_name", nullable = false, length = 40)
    private String clusterName;

    @Column(name = "municipality", nullable = false, length = 60)
    private String municipality;

    @Column(name = "day_date", nullable = false)
    private LocalDate dayDate;

    @Column(name = "period", nullable = false, length = 16)
    private String period;

    @Column(name = "active_users", nullable = false)
    private Long activeUsers;

    @Column(name = "sessions", nullable = false)
    private Long sessions;

    @Column(name = "download_bytes", nullable = false)
    private Long downloadBytes;

    @Column(name = "upload_bytes", nullable = false)
    private Long uploadBytes;

    @Column(name = "average_session_duration_seconds", nullable = false)
    private Integer averageSessionDurationSeconds;

    @Column(name = "average_drop_rate", nullable = false, precision = 7, scale = 6)
    private BigDecimal averageDropRate;

    @Column(name = "average_congestion", nullable = false, precision = 7, scale = 6)
    private BigDecimal averageCongestion;

    @Column(name = "total_calls", nullable = false)
    private Integer totalCalls;

    @Column(name = "total_messages", nullable = false)
    private Integer totalMessages;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
