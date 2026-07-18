package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;

public interface CsvIngestService {

    String supportedFileName();

    CsvIngestResult ingest(DatasetObjectKey key);
}
