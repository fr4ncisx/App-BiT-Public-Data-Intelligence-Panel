package com.appbit.geoanalytics.infrastructure.adapter.out.storage.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class R2StoragePropertiesTest {

    @Test
    void normalizesCsvPrefix() {
        R2StorageProperties properties = new R2StorageProperties(
                " http://localhost:9000 ",
                " access-key ",
                " secret-key ",
                " appbit-datasets ",
                " auto ",
                " /raw/visent-cdrview/ "
        );

        assertThat(properties.endpoint()).isEqualTo("http://localhost:9000");
        assertThat(properties.accessKeyId()).isEqualTo("access-key");
        assertThat(properties.secretAccessKey()).isEqualTo("secret-key");
        assertThat(properties.bucketName()).isEqualTo("appbit-datasets");
        assertThat(properties.region()).isEqualTo("auto");
        assertThat(properties.csvPrefix()).isEqualTo("raw/visent-cdrview");
    }

    @Test
    void rejectsInvalidEndpoint() {
        assertThatThrownBy(() -> new R2StorageProperties(
                "ftp://localhost:9000",
                "access-key",
                "secret-key",
                "appbit-datasets",
                "auto",
                "raw/visent-cdrview"
        )).isInstanceOf(R2StoragePropertiesException.class)
                .hasMessage("appbit.storage.r2.endpoint must use http or https");
    }

    @Test
    void rejectsBucketNameWithPathSeparators() {
        assertThatThrownBy(() -> new R2StorageProperties(
                "http://localhost:9000",
                "access-key",
                "secret-key",
                "appbit/datasets",
                "auto",
                "raw/visent-cdrview"
        )).isInstanceOf(R2StoragePropertiesException.class)
                .hasMessage("appbit.storage.r2.bucket-name must not contain path separators");
    }
}
