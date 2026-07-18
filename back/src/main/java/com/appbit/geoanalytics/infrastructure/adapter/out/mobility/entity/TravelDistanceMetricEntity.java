package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity;

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
@Table(name = "travel_distance_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelDistanceMetricEntity {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "origin_region_id", nullable = false)
    private UUID originRegionId;

    @Column(name = "destination_region_id", nullable = false)
    private UUID destinationRegionId;

    @Column(name = "origin_cluster_name", nullable = false, length = 40)
    private String originClusterName;

    @Column(name = "destination_cluster_name", nullable = false, length = 40)
    private String destinationClusterName;

    @Column(name = "same_cluster", nullable = false)
    private Boolean sameCluster;

    @Column(name = "observations", nullable = false)
    private Long observations;

    @Column(name = "average_distance_km", nullable = false, precision = 10, scale = 3)
    private BigDecimal averageDistanceKm;

    @Column(name = "p25_distance_km", nullable = false, precision = 10, scale = 3)
    private BigDecimal p25DistanceKm;

    @Column(name = "p75_distance_km", nullable = false, precision = 10, scale = 3)
    private BigDecimal p75DistanceKm;

    @Column(name = "predominant_period", nullable = false, length = 16)
    private String predominantPeriod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
