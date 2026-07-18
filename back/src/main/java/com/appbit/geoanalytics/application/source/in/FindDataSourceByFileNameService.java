package com.appbit.geoanalytics.application.source.in;

import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindDataSourceByFileNameService implements FindDataSourceByFileNameUseCase {

    private final DataSourcePort dataSourcePort;

    @Override
    public SourceCatalogEntry execute(SourceFileName fileName) {
        return dataSourcePort.findByFileName(fileName)
                .orElseThrow(() -> new DataSourceNotFoundException(fileName.value()));
    }
}
