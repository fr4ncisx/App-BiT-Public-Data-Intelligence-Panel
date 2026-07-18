package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.SourceSummaryDTO;
import com.appbit.geoanalytics.application.catalog.out.SourceCatalogPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SourceCatalogRepositoryAdapter implements SourceCatalogPort {

    private final DataSourceJpaRepository dataSourceJpaRepository;

    @Override
    public List<SourceSummaryDTO> findAll() {
        return dataSourceJpaRepository.findAll().stream()
                .map(entity -> new SourceSummaryDTO(
                        entity.getSourceName(),
                        entity.getFileName(),
                        entity.getSourceType()))
                .toList();
    }
}
