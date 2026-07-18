package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.RegionsMapResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.RegionsMapResponse.RegionMapDTO;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegionMapRepositoryAdapter implements RegionMapPort {

    private final RegionJpaRepository regionJpaRepository;

    @Override
    public List<RegionMapDTO> findAll() {
        return regionJpaRepository.findAll().stream()
                .map(this::toRegionMapDTO)
                .toList();
    }

    private RegionMapDTO toRegionMapDTO(RegionEntity entity) {
        return new RegionMapDTO(
                entity.getId(),
                entity.getRegionCode(),
                entity.getRegionName(),
                entity.getMunicipality(),
                new GeoPointDTO(entity.getCenterLatitude(), entity.getCenterLongitude()),
                null
        );
    }
}
