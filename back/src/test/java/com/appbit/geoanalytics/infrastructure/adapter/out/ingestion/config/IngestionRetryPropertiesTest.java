package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IngestionRetryPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(PropertiesConfig.class);

    @EnableConfigurationProperties(IngestionRetryProperties.class)
    @Configuration(proxyBeanMethods = false)
    static class PropertiesConfig {
    }

    @Test
    void bindsProvidedValues() {
        contextRunner
                .withPropertyValues(
                        "appbit.ingestion.retry.max-attempts=5",
                        "appbit.ingestion.retry.base-delay-ms=750")
                .run(context -> {
                    var properties = context.getBean(IngestionRetryProperties.class);

                    assertThat(properties.maxAttempts()).isEqualTo(5);
                    assertThat(properties.baseDelayMs()).isEqualTo(750);
                });
    }

    @Test
    void appliesDefaultValuesWhenNotConfigured() {
        contextRunner.run(context -> {
            var properties = context.getBean(IngestionRetryProperties.class);

            assertThat(properties.maxAttempts()).isEqualTo(3);
            assertThat(properties.baseDelayMs()).isEqualTo(2000);
        });
    }

    @Test
    void wiresOrchestratorWithRetryProperties() {
        new ApplicationContextRunner()
                .withUserConfiguration(SupportConfig.class, PropertiesConfig.class, IngestionOrchestratorConfig.class)
                .withBean(DatasetObjectStoragePort.class, () -> mock(DatasetObjectStoragePort.class))
                .withBean(DataSourcePort.class, () -> mock(DataSourcePort.class))
                .withBean(IngestionRunPort.class, () -> mock(IngestionRunPort.class))
                .withBean(IngestionSkipProperties.class, () -> new IngestionSkipProperties(Set.of()))
                .withPropertyValues(
                        "appbit.ingestion.retry.max-attempts=7",
                        "appbit.ingestion.retry.base-delay-ms=500")
                .run(context -> {
                    var orchestrator = context.getBean(com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator.class);

                    assertThat(orchestrator).isNotNull();
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class SupportConfig {

        @Bean
        Executor taskExecutor() {
            return Runnable::run;
        }
    }
}