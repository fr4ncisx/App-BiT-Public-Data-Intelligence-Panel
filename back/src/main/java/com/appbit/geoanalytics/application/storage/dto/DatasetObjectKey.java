package com.appbit.geoanalytics.application.storage.dto;

import com.appbit.geoanalytics.application.storage.exception.DatasetObjectKeyException;

public record DatasetObjectKey(String value) {

    public DatasetObjectKey {
        if (value == null) {
            throw new DatasetObjectKeyException("Dataset object key cannot be null");
        }

        value = value.strip();

        if (value.isBlank()) {
            throw new DatasetObjectKeyException("Dataset object key cannot be blank");
        }

        if (value.startsWith("/")) {
            throw new DatasetObjectKeyException("Dataset object key cannot start with a slash");
        }

        if (value.contains("\\")) {
            throw new DatasetObjectKeyException("Dataset object key cannot contain backslashes");
        }

        if (value.contains("..")) {
            throw new DatasetObjectKeyException("Dataset object key cannot contain path traversal");
        }
    }
}
