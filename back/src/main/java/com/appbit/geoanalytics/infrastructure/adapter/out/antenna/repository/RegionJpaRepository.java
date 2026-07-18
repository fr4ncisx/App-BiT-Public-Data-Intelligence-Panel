package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionJpaRepository extends JpaRepository<RegionEntity, UUID> {

    Optional<RegionEntity> findByClusterNameAndMunicipality(String clusterName, String municipality);

    Optional<RegionEntity> findByClusterName(String clusterName);

    Optional<RegionEntity> findByRegionCode(String regionCode);

    @Modifying
    @Query(value = """
            INSERT INTO regions (id, region_code, region_name, cluster_name, municipality, center_latitude, center_longitude, created_at)
            VALUES (:id, :regionCode, :regionName, :clusterName, :municipality, :centerLatitude, :centerLongitude, :createdAt)
            ON CONFLICT (region_code) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("regionCode") String regionCode,
            @Param("regionName") String regionName,
            @Param("clusterName") String clusterName,
            @Param("municipality") String municipality,
            @Param("centerLatitude") BigDecimal centerLatitude,
            @Param("centerLongitude") BigDecimal centerLongitude,
            @Param("createdAt") Instant createdAt
    );
}
