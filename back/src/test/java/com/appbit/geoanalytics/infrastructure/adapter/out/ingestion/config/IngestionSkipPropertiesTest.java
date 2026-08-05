package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;


import static org.assertj.core.api.Assertions.assertThat;

class IngestionSkipPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(IngestionSkipProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class PropertiesConfig {
    }

    @Test
    void bindsProvidedSkipFiles() {
        contextRunner
                .withPropertyValues("appbit.ingestion.skip-files=trajetos_comuns.csv,assinantes.csv")
                .run(context -> {
                    var properties = context.getBean(IngestionSkipProperties.class);

                    assertThat(properties.skipFiles())
                            .containsExactlyInAnyOrder("trajetos_comuns.csv", "assinantes.csv");
                });
    }

    @Test
    void appliesDefaultSkipFilesWhenNotConfigured() {
        contextRunner.run(context -> {
            var properties = context.getBean(IngestionSkipProperties.class);

            assertThat(properties.skipFiles()).contains(
                    "tensor_mobilidade.csv",
                    "tensor_sequencias.csv",
                    "network_indicators_seed.csv",
                    "trajetos_comuns.csv",
                    "assinantes.csv",
                    "sumario_kanon.csv");
        });
    }

    @Test
    void normalizesBlankAndEmptyEntries() {
        contextRunner
                .withPropertyValues("appbit.ingestion.skip-files=tenant.csv, ,another.csv")
                .run(context -> {
                    var properties = context.getBean(IngestionSkipProperties.class);

                    assertThat(properties.skipFiles())
                            .containsExactlyInAnyOrder("tenant.csv", "another.csv");
                });
    }

    @Test
    void emptySkipFilesWhenExplicitlyEmpty() {
        contextRunner
                .withPropertyValues("appbit.ingestion.skip-files=")
                .run(context -> {
                    var properties = context.getBean(IngestionSkipProperties.class);

                    assertThat(properties.skipFiles()).isEmpty();
                });
    }
}