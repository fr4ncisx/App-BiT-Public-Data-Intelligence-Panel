package com.appbit.geoanalytics.application.storage.dto;

import com.appbit.geoanalytics.application.storage.exception.DatasetObjectKeyException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatasetObjectKeyTest {

    @Test
    void trimsValidKey() {
        DatasetObjectKey key = new DatasetObjectKey(" antenas_flp.csv ");

        assertThat(key.value()).isEqualTo("antenas_flp.csv");
    }

    @Test
    void rejectsInvalidKeys() {
        assertThatThrownBy(() -> new DatasetObjectKey(null))
                .isInstanceOf(DatasetObjectKeyException.class)
                .hasMessage("Dataset object key cannot be null");
        assertThatThrownBy(() -> new DatasetObjectKey("   "))
                .isInstanceOf(DatasetObjectKeyException.class)
                .hasMessage("Dataset object key cannot be blank");
        assertThatThrownBy(() -> new DatasetObjectKey("/antenas_flp.csv"))
                .isInstanceOf(DatasetObjectKeyException.class)
                .hasMessage("Dataset object key cannot start with a slash");
        assertThatThrownBy(() -> new DatasetObjectKey("raw\\antenas_flp.csv"))
                .isInstanceOf(DatasetObjectKeyException.class)
                .hasMessage("Dataset object key cannot contain backslashes");
        assertThatThrownBy(() -> new DatasetObjectKey("../antenas_flp.csv"))
                .isInstanceOf(DatasetObjectKeyException.class)
                .hasMessage("Dataset object key cannot contain path traversal");
    }
}
