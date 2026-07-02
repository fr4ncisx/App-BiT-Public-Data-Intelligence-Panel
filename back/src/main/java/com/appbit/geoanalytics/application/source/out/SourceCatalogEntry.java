package com.appbit.geoanalytics.application.source.out;

import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;

import java.util.UUID;

public record SourceCatalogEntry(
        UUID id,
        String sourceName,
        SourceFileName fileName,
        DataSourceType sourceType,
        String description
) {
}
