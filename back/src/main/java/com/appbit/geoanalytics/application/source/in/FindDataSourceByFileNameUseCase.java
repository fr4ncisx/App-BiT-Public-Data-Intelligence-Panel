package com.appbit.geoanalytics.application.source.in;

import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;

public interface FindDataSourceByFileNameUseCase {

    SourceCatalogEntry execute(SourceFileName fileName);
}
