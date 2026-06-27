package com.appbit.geoanalytics.application.storage.dto;

import com.appbit.geoanalytics.application.storage.exception.DatasetObjectMetadataException;

public record DatasetObjectMetadata(
        DatasetObjectKey key,
        long contentLength,
        String eTag,
        String contentType
) {

    public DatasetObjectMetadata {
        if (key == null) {
            throw new DatasetObjectMetadataException("Dataset object key cannot be null");
        }

        if (contentLength < 0) {
            throw new DatasetObjectMetadataException("Dataset object content length cannot be negative");
        }
    }
}
