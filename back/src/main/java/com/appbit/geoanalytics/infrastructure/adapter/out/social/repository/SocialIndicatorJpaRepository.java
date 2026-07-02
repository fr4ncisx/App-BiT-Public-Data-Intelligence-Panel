package com.appbit.geoanalytics.infrastructure.adapter.out.social.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.social.entity.SocialIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface SocialIndicatorJpaRepository extends JpaRepository<SocialIndicatorEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO social_indicators (id, region_id, source_id, indicator_type, score, unit, gap_level, confidence_level, description, created_at)
            VALUES (:id, :regionId, :sourceId, :indicatorType, :score, :unit, :gapLevel, :confidenceLevel, :description, :createdAt)
            ON CONFLICT (region_id, source_id, indicator_type) DO NOTHING
            """, nativeQuery = true)
    void insertIgnoreConflict(
            @Param("id") UUID id,
            @Param("regionId") UUID regionId,
            @Param("sourceId") UUID sourceId,
            @Param("indicatorType") String indicatorType,
            @Param("score") BigDecimal score,
            @Param("unit") String unit,
            @Param("gapLevel") String gapLevel,
            @Param("confidenceLevel") String confidenceLevel,
            @Param("description") String description,
            @Param("createdAt") Instant createdAt
    );
}
