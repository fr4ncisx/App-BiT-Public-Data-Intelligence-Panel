package com.appbit.geoanalytics.application.source.out;

import com.appbit.geoanalytics.domain.source.vo.SourceFileName;

import java.util.Optional;

public interface DataSourcePort {

    Optional<SourceCatalogEntry> findByFileName(SourceFileName fileName);
}
