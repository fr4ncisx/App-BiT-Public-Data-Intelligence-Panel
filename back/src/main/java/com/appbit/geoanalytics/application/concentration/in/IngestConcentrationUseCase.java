package com.appbit.geoanalytics.application.concentration.in;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;

public interface IngestConcentrationUseCase {

    IngestConcentrationResult execute(DatasetObjectKey key);
}
