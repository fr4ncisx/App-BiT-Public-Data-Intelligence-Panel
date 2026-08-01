package com.appbit.geoanalytics.infrastructure.adapter.out.source.adapter;

import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.mapper.DataSourceMapper;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSourceRepositoryAdapter implements DataSourcePort {

    private final DataSourceJpaRepository repository;
    private final DataSourceMapper mapper;

    @Override
    public Optional<SourceCatalogEntry> findByFileName(SourceFileName fileName) {
        return repository.findByFileName(fileName.value()).map(mapper::toSourceCatalogEntry);
    }

    @Override
    public List<SourceCatalogEntry> findAll() {
        return mapper.toSourceCatalogEntryList(repository.findAll());
    }
}
