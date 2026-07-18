package com.appbit.geoanalytics.application.source.out;

import com.appbit.geoanalytics.domain.source.enums.ConfidenceLevel;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.enums.GovernanceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record SourceCatalogEntry(
        UUID id,
        String sourceName,
        SourceFileName fileName,
        DataSourceType sourceType,
        String description,
        @Nullable ConfidenceLevel confidenceLevel,
        @Nullable String periodStart,
        @Nullable String periodEnd,
        @Nullable GovernanceType governanceType
) {
}
