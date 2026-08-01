package com.appbit.geoanalytics.infrastructure.adapter.out.source.mapper;

import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.enums.ConfidenceLevel;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.enums.GovernanceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DataSourceMapper {

    SourceCatalogEntry toSourceCatalogEntry(DataSourceEntity entity);

    List<SourceCatalogEntry> toSourceCatalogEntryList(List<DataSourceEntity> entities);

    default SourceFileName toSourceFileName(String value) {
        return value == null ? null : new SourceFileName(value);
    }

    default DataSourceType toDataSourceType(String value) {
        return value == null ? null : DataSourceType.valueOf(value);
    }

    default ConfidenceLevel toConfidenceLevel(String value) {
        return value == null ? null : ConfidenceLevel.valueOf(value);
    }

    default GovernanceType toGovernanceType(String value) {
        return value == null ? null : GovernanceType.valueOf(value);
    }
}
