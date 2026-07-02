package com.appbit.geoanalytics.application.antenna.in;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;

public interface IngestAntennasUseCase {

    AntennaIngestResult execute(DatasetObjectKey key);
}
