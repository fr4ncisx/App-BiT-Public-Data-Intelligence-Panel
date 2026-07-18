package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.TravelDistanceMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface TravelDistanceMetricJpaRepository extends JpaRepository<TravelDistanceMetricEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO travel_distance_metrics (id, source_id, origin_region_id, destination_region_id,
                origin_cluster_name, destination_cluster_name, same_cluster, observations,
                average_distance_km, p25_distance_km, p75_distance_km, predominant_period, created_at)
            VALUES (:id, :sourceId, :originRegionId, :destinationRegionId,
                :originClusterName, :destinationClusterName, :sameCluster, :observations,
                :averageDistanceKm, :p25DistanceKm, :p75DistanceKm, :predominantPeriod, :createdAt)
            ON CONFLICT (source_id, origin_region_id, destination_region_id, predominant_period) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("sourceId") UUID sourceId,
            @Param("originRegionId") UUID originRegionId,
            @Param("destinationRegionId") UUID destinationRegionId,
            @Param("originClusterName") String originClusterName,
            @Param("destinationClusterName") String destinationClusterName,
            @Param("sameCluster") Boolean sameCluster,
            @Param("observations") Long observations,
            @Param("averageDistanceKm") BigDecimal averageDistanceKm,
            @Param("p25DistanceKm") BigDecimal p25DistanceKm,
            @Param("p75DistanceKm") BigDecimal p75DistanceKm,
            @Param("predominantPeriod") String predominantPeriod,
            @Param("createdAt") Instant createdAt
    );
}
