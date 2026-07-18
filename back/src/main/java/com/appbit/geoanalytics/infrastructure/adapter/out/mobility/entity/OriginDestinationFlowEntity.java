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
@Table(name = "origin_destination_flows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OriginDestinationFlowEntity {

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

    @Column(name = "origin_municipality", nullable = false, length = 60)
    private String originMunicipality;

    @Column(name = "destination_municipality", nullable = false, length = 60)
    private String destinationMunicipality;

    @Column(name = "origin_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal originLatitude;

    @Column(name = "origin_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal originLongitude;

    @Column(name = "destination_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal destinationLatitude;

    @Column(name = "destination_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal destinationLongitude;

    @Column(name = "same_cluster", nullable = false)
    private Boolean sameCluster;

    @Column(name = "users_count", nullable = false)
    private Long usersCount;

    @Column(name = "trips_count", nullable = false)
    private Long tripsCount;

    @Column(name = "average_distance_km", nullable = false, precision = 10, scale = 3)
    private BigDecimal averageDistanceKm;

    @Column(name = "predominant_period", nullable = false, length = 16)
    private String predominantPeriod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
