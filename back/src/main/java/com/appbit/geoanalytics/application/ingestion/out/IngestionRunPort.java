package com.appbit.geoanalytics.application.ingestion.out;

import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;

public interface IngestionRunPort {

    IngestionRun save(IngestionRun run);
}
