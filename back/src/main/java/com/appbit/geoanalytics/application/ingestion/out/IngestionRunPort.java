package com.appbit.geoanalytics.application.ingestion.out;

import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;

import java.util.Optional;
import java.util.UUID;

public interface IngestionRunPort {

    IngestionRun save(IngestionRun run);

    Optional<IngestionRun> findLatestBySourceIdAndFileName(UUID sourceId, String fileName);
}
