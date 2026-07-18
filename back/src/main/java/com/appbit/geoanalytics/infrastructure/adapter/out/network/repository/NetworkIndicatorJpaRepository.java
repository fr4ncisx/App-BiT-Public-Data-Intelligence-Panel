package com.appbit.geoanalytics.infrastructure.adapter.out.network.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.network.entity.NetworkIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NetworkIndicatorJpaRepository extends JpaRepository<NetworkIndicatorEntity, UUID> {

    @Query("SELECT n.regionId, n.indicatorType, n.score, n.unit FROM NetworkIndicatorEntity n WHERE n.regionId IN :regionIds")
    List<Object[]> findNetworkIndicatorsByRegionIds(@Param("regionIds") List<UUID> regionIds);
}
