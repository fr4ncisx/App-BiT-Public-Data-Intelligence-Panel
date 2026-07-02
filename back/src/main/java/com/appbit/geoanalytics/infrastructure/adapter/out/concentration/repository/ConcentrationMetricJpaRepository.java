package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.entity.ConcentrationMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ConcentrationMetricJpaRepository extends JpaRepository<ConcentrationMetricEntity, UUID> {

    @Query("SELECT DISTINCT c.ecgi FROM ConcentrationMetricEntity c")
    Set<String> findAllDistinctEcgis();

    @Modifying
    @Query(value = """
            INSERT INTO concentration_metrics (id, source_id, region_id, ecgi, cluster_name, municipality, day_date, period, active_users, sessions, download_bytes, upload_bytes, average_session_duration_seconds, average_drop_rate, average_congestion, total_calls, total_messages, latitude, longitude, created_at)
            VALUES (:id, :sourceId, :regionId, :ecgi, :clusterName, :municipality, :dayDate, :period, :activeUsers, :sessions, :downloadBytes, :uploadBytes, :averageSessionDurationSeconds, :averageDropRate, :averageCongestion, :totalCalls, :totalMessages, :latitude, :longitude, :createdAt)
            ON CONFLICT (source_id, ecgi, day_date, period) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("sourceId") UUID sourceId,
            @Param("regionId") UUID regionId,
            @Param("ecgi") String ecgi,
            @Param("clusterName") String clusterName,
            @Param("municipality") String municipality,
            @Param("dayDate") LocalDate dayDate,
            @Param("period") String period,
            @Param("activeUsers") Long activeUsers,
            @Param("sessions") Long sessions,
            @Param("downloadBytes") Long downloadBytes,
            @Param("uploadBytes") Long uploadBytes,
            @Param("averageSessionDurationSeconds") Integer averageSessionDurationSeconds,
            @Param("averageDropRate") BigDecimal averageDropRate,
            @Param("averageCongestion") BigDecimal averageCongestion,
            @Param("totalCalls") Integer totalCalls,
            @Param("totalMessages") Integer totalMessages,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("createdAt") Instant createdAt
    );
}
