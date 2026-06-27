package com.appbit.geoanalytics.application.storage.port.out;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectMetadata;

import java.io.InputStream;

public interface DatasetObjectStoragePort {

    boolean exists(DatasetObjectKey key);

    DatasetObjectMetadata metadata(DatasetObjectKey key);

    InputStream openStream(DatasetObjectKey key);
}
