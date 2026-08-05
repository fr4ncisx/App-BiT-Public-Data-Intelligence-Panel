package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IngestionPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(IngestionProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class PropertiesConfig {
    }

    @Test
    void bindsProvidedValues() {
        contextRunner
                .withPropertyValues(
                        "appbit.ingestion.enabled=false",
                        "appbit.ingestion.batch-size=1000",
                        "appbit.ingestion.fail-fast=false")
                .run(context -> {
                    var properties = context.getBean(IngestionProperties.class);

                    assertThat(properties.enabled()).isFalse();
                    assertThat(properties.batchSize()).isEqualTo(1000);
                    assertThat(properties.failFast()).isFalse();
                });
    }

    @Test
    void appliesDefaultValuesWhenNotConfigured() {
        contextRunner.run(context -> {
            var properties = context.getBean(IngestionProperties.class);

            assertThat(properties.enabled()).isTrue();
            assertThat(properties.batchSize()).isEqualTo(500);
            assertThat(properties.failFast()).isTrue();
        });
    }

    @Test
    void rejectsZeroBatchSize() {
        assertThatThrownBy(() -> new IngestionProperties(true, 0, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBatchSize() {
        assertThatThrownBy(() -> new IngestionProperties(true, -1, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
