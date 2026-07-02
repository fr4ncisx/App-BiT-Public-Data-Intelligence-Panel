package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.AntennaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AntennaJpaRepository extends JpaRepository<AntennaEntity, UUID> {

    List<AntennaEntity> findByEcgiIn(Collection<String> ecgiValues);

    boolean existsByEcgi(String ecgi);

    @Query("SELECT a.ecgi FROM AntennaEntity a")
    Set<String> findAllEcgis();

    @Modifying
    @Query(value = """
            INSERT INTO antennas (id, ecgi, region_id, cluster_name, municipality, latitude, longitude, source_id, created_at)
            VALUES (:id, :ecgi, :regionId, :clusterName, :municipality, :latitude, :longitude, :sourceId, :createdAt)
            ON CONFLICT (ecgi) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("ecgi") String ecgi,
            @Param("regionId") UUID regionId,
            @Param("clusterName") String clusterName,
            @Param("municipality") String municipality,
            @Param("latitude") java.math.BigDecimal latitude,
            @Param("longitude") java.math.BigDecimal longitude,
            @Param("sourceId") UUID sourceId,
            @Param("createdAt") java.time.Instant createdAt
    );
}
