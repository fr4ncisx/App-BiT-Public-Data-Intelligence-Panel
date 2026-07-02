package com.appbit.geoanalytics.application.social.in;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;

public interface IngestSocialIndicatorsUseCase {

    IngestSocialIndicatorsResult execute(DatasetObjectKey key);
}
