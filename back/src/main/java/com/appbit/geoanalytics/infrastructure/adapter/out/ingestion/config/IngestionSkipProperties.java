package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "appbit.ingestion")
public record IngestionSkipProperties(
        @DefaultValue({
                "tensor_mobilidade.csv",
                "tensor_sequencias.csv",
                "network_indicators_seed.csv",
                "trajetos_comuns.csv",
                "assinantes.csv",
                "sumario_kanon.csv"
        })
        Set<String> skipFiles
) {
    public IngestionSkipProperties {
        skipFiles = skipFiles.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}