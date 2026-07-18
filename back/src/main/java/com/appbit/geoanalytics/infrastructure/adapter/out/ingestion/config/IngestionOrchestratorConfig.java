package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config;

import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator;
import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestratorService;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class IngestionOrchestratorConfig {

    @Bean
    public IngestionOrchestrator ingestionOrchestrator(
            DatasetObjectStoragePort storagePort,
            DataSourcePort dataSourcePort,
            IngestionRunPort ingestionRunPort,
            List<CsvIngestService> csvIngestServices
    ) {
        return new IngestionOrchestratorService(storagePort, dataSourcePort, ingestionRunPort, csvIngestServices);
    }
}
