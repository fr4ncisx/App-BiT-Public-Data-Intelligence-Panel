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
@Table(name = "antennas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AntennaEntity {

    @Id
    private UUID id;

    @Column(name = "ecgi", nullable = false, length = 16, unique = true)
    private String ecgi;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

    @Column(name = "cluster_name", nullable = false, length = 40)
    private String clusterName;

    @Column(name = "municipality", nullable = false, length = 60)
    private String municipality;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
