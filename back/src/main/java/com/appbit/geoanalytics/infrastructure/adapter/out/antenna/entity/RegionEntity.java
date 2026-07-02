package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity;

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
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RegionEntity {

    @Id
    private UUID id;

    @Column(name = "region_code", nullable = false, length = 80, unique = true)
    private String regionCode;

    @Column(name = "region_name", nullable = false, length = 80)
    private String regionName;

    @Column(name = "municipality", nullable = false, length = 60)
    private String municipality;

    @Column(name = "cluster_name", nullable = false, length = 40)
    private String clusterName;

    @Column(name = "center_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLatitude;

    @Column(name = "center_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLongitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
