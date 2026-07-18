package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.OriginDestinationFlowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OriginDestinationFlowJpaRepository extends JpaRepository<OriginDestinationFlowEntity, UUID> {

    @Query("""
            SELECT f.originRegionId, f.destinationRegionId, f.originClusterName, f.destinationClusterName,
                   f.originMunicipality, f.destinationMunicipality,
                   f.originLatitude, f.originLongitude, f.destinationLatitude, f.destinationLongitude,
                   f.sameCluster, f.usersCount, f.tripsCount, f.averageDistanceKm, f.predominantPeriod
            FROM OriginDestinationFlowEntity f
            WHERE f.originRegionId IN :regionIds OR f.destinationRegionId IN :regionIds
            """)
    List<Object[]> findFlowsByRegionIds(@Param("regionIds") List<UUID> regionIds);

    @Modifying
    @Query(value = """
            INSERT INTO origin_destination_flows (id, source_id, origin_region_id, destination_region_id,
                origin_cluster_name, destination_cluster_name, origin_municipality, destination_municipality,
                origin_latitude, origin_longitude, destination_latitude, destination_longitude,
                same_cluster, users_count, trips_count, average_distance_km, predominant_period, created_at)
            VALUES (:id, :sourceId, :originRegionId, :destinationRegionId,
                :originClusterName, :destinationClusterName, :originMunicipality, :destinationMunicipality,
                :originLatitude, :originLongitude, :destinationLatitude, :destinationLongitude,
                :sameCluster, :usersCount, :tripsCount, :averageDistanceKm, :predominantPeriod, :createdAt)
            ON CONFLICT (source_id, origin_region_id, destination_region_id, predominant_period) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("sourceId") UUID sourceId,
            @Param("originRegionId") UUID originRegionId,
            @Param("destinationRegionId") UUID destinationRegionId,
            @Param("originClusterName") String originClusterName,
            @Param("destinationClusterName") String destinationClusterName,
            @Param("originMunicipality") String originMunicipality,
            @Param("destinationMunicipality") String destinationMunicipality,
            @Param("originLatitude") BigDecimal originLatitude,
            @Param("originLongitude") BigDecimal originLongitude,
            @Param("destinationLatitude") BigDecimal destinationLatitude,
            @Param("destinationLongitude") BigDecimal destinationLongitude,
            @Param("sameCluster") Boolean sameCluster,
            @Param("usersCount") Long usersCount,
            @Param("tripsCount") Long tripsCount,
            @Param("averageDistanceKm") BigDecimal averageDistanceKm,
            @Param("predominantPeriod") String predominantPeriod,
            @Param("createdAt") Instant createdAt
    );
}
