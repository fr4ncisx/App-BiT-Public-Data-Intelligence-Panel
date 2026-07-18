package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.RegionSummaryDTO;
import com.appbit.geoanalytics.application.catalog.out.RegionCatalogPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegionCatalogRepositoryAdapter implements RegionCatalogPort {

    private final RegionJpaRepository regionJpaRepository;

    @Override
    public List<RegionSummaryDTO> findAll() {
        return regionJpaRepository.findAll().stream()
                .map(entity -> new RegionSummaryDTO(
                        entity.getRegionCode(),
                        entity.getRegionName(),
                        entity.getMunicipality()))
                .toList();
    }
}
