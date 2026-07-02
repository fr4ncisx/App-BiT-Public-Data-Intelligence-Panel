package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionJpaRepository extends JpaRepository<RegionEntity, UUID> {

    Optional<RegionEntity> findByClusterNameAndMunicipality(String clusterName, String municipality);

    Optional<RegionEntity> findByRegionCode(String regionCode);
}
