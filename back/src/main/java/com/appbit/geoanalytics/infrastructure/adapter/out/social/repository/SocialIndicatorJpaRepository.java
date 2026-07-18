package com.appbit.geoanalytics.infrastructure.adapter.out.social.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.social.entity.SocialIndicatorEntity;
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
public interface SocialIndicatorJpaRepository extends JpaRepository<SocialIndicatorEntity, UUID> {

    @Query("SELECT DISTINCT s.indicatorType FROM SocialIndicatorEntity s ORDER BY s.indicatorType")
    List<String> findDistinctIndicatorTypes();

    @Query("SELECT s.regionId, s.indicatorType, s.score, s.unit FROM SocialIndicatorEntity s WHERE s.regionId IN :regionIds")
    List<Object[]> findSocialIndicatorsByRegionIds(@Param("regionIds") List<UUID> regionIds);

    @Query("SELECT s.regionId, s.indicatorType, s.score, s.unit FROM SocialIndicatorEntity s WHERE s.regionId IN :regionIds AND s.indicatorType = :indicatorType")
    List<Object[]> findSocialIndicatorsByRegionIdsAndType(@Param("regionIds") List<UUID> regionIds, @Param("indicatorType") String indicatorType);

    @Query("""
            SELECT s.regionId, s.indicatorType, s.score, s.unit, s.gapLevel, s.confidenceLevel, s.description,
                   r.regionCode, r.regionName, r.municipality
            FROM SocialIndicatorEntity s, RegionEntity r
            WHERE s.regionId = r.id AND s.indicatorType = :indicatorType
            ORDER BY r.regionName
            """)
    List<Object[]> findSocialGapByIndicatorType(@Param("indicatorType") String indicatorType);

    @Query(value = """
            SELECT s.region_id, s.indicator_type, s.score, s.unit, s.gap_level, s.confidence_level, s.description,
                   r.region_code, r.region_name, r.municipality, ds.source_type
            FROM social_indicators s, regions r, data_sources ds
            WHERE s.region_id = r.id AND s.source_id = ds.id AND s.indicator_type = :indicatorType
            ORDER BY r.region_name
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findSocialGapByIndicatorTypePaginated(@Param("indicatorType") String indicatorType,
                                                          @Param("limit") int limit,
                                                          @Param("offset") int offset);

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
