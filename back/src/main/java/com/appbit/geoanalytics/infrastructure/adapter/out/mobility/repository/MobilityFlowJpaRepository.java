package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.MobilityFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface MobilityFlowJpaRepository extends JpaRepository<MobilityFlowEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO mobility_flows (id, source_id, origin_region_id, destination_region_id, origin_ecgi, destination_ecgi,
                origin_latitude, origin_longitude, destination_latitude, destination_longitude,
                origin_cluster_name, destination_cluster_name, origin_municipality, destination_municipality,
                users_count, transitions_count, distance_km, predominant_period, origin_cluster_percentage, created_at)
            VALUES (:id, :sourceId, :originRegionId, :destinationRegionId, :originEcgi, :destinationEcgi,
                :originLatitude, :originLongitude, :destinationLatitude, :destinationLongitude,
                :originClusterName, :destinationClusterName, :originMunicipality, :destinationMunicipality,
                :usersCount, :transitionsCount, :distanceKm, :predominantPeriod, :originClusterPercentage, :createdAt)
            ON CONFLICT (source_id, origin_ecgi, destination_ecgi, predominant_period) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("sourceId") UUID sourceId,
            @Param("originRegionId") UUID originRegionId,
            @Param("destinationRegionId") UUID destinationRegionId,
            @Param("originEcgi") String originEcgi,
            @Param("destinationEcgi") String destinationEcgi,
            @Param("originLatitude") BigDecimal originLatitude,
            @Param("originLongitude") BigDecimal originLongitude,
            @Param("destinationLatitude") BigDecimal destinationLatitude,
            @Param("destinationLongitude") BigDecimal destinationLongitude,
            @Param("originClusterName") String originClusterName,
            @Param("destinationClusterName") String destinationClusterName,
            @Param("originMunicipality") String originMunicipality,
            @Param("destinationMunicipality") String destinationMunicipality,
            @Param("usersCount") Long usersCount,
            @Param("transitionsCount") Long transitionsCount,
            @Param("distanceKm") BigDecimal distanceKm,
            @Param("predominantPeriod") String predominantPeriod,
            @Param("originClusterPercentage") BigDecimal originClusterPercentage,
            @Param("createdAt") Instant createdAt
    );
}
