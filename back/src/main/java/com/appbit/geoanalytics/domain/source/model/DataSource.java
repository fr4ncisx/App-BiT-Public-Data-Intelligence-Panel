package com.appbit.geoanalytics.domain.source.model;

import com.appbit.geoanalytics.domain.source.enums.ConfidenceLevel;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.enums.GovernanceType;
import com.appbit.geoanalytics.domain.source.exception.SourceDomainException;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public final class DataSource {

    private final DataSourceId id;
    private final String sourceName;
    private final SourceFileName fileName;
    private final DataSourceType sourceType;
    private final String description;
    private final @Nullable ConfidenceLevel confidenceLevel;
    private final @Nullable String periodStart;
    private final @Nullable String periodEnd;
    private final @Nullable GovernanceType governanceType;

    @Builder
    public DataSource(
            DataSourceId id,
            String sourceName,
            SourceFileName fileName,
            DataSourceType sourceType,
            String description,
            @Nullable ConfidenceLevel confidenceLevel,
            @Nullable String periodStart,
            @Nullable String periodEnd,
            @Nullable GovernanceType governanceType
    ) {
        if (id == null) {
            throw new SourceDomainException("Data source id cannot be null");
        }

        if (fileName == null) {
            throw new SourceDomainException("Source file name cannot be null");
        }

        if (sourceType == null) {
            throw new SourceDomainException("Data source type cannot be null");
        }

        this.id = id;
        this.sourceName = validateText(sourceName, "Source name", 2, 120);
        this.fileName = fileName;
        this.sourceType = sourceType;
        this.description = validateText(description, "Description", 5, 500);
        this.confidenceLevel = confidenceLevel;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.governanceType = governanceType;
    }

    private String validateText(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            throw new SourceDomainException(fieldName + " cannot be null");
        }

        String trimmed = value.trim();

        if (trimmed.isBlank()) {
            throw new SourceDomainException(fieldName + " cannot be blank");
        }

        if (trimmed.length() < minLength || trimmed.length() > maxLength) {
            throw new SourceDomainException(
                    fieldName + " length must be between " + minLength + " and " + maxLength + " characters"
            );
        }

        if (trimmed.chars().anyMatch(Character::isISOControl)) {
            throw new SourceDomainException(fieldName + " cannot contain control characters");
        }

        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSource that = (DataSource) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}