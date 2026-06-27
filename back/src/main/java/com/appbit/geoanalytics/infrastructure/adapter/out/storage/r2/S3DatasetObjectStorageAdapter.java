package com.appbit.geoanalytics.infrastructure.adapter.out.storage.r2;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectMetadata;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectNotFoundException;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectStorageCredentialsException;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectStorageException;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.infrastructure.adapter.out.storage.properties.R2StorageProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.InputStream;

@Component
public class S3DatasetObjectStorageAdapter implements DatasetObjectStoragePort {

    private static final int NOT_FOUND_STATUS = 404;
    private static final int UNAUTHORIZED_STATUS = 401;
    private static final int FORBIDDEN_STATUS = 403;

    private final S3Client s3Client;
    private final R2StorageProperties properties;

    public S3DatasetObjectStorageAdapter(S3Client s3Client, R2StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public boolean exists(DatasetObjectKey key) {
        try {
            s3Client.headObject(headObjectRequest(key));
            return true;
        } catch (AwsServiceException exception) {
            if (isUnauthorized(exception)) {
                throw new DatasetObjectStorageCredentialsException("Invalid credentials for storage object: " + key.value());
            }

            if (isNotFound(exception)) {
                return false;
            }

            throw new DatasetObjectStorageException("Cannot verify dataset object existence: " + key.value(), exception);
        }
    }

    @Override
    public DatasetObjectMetadata metadata(DatasetObjectKey key) {
        try {
            HeadObjectResponse response = s3Client.headObject(headObjectRequest(key));
            return new DatasetObjectMetadata(key, response.contentLength(), response.eTag(), response.contentType());
        } catch (AwsServiceException exception) {
            if (isUnauthorized(exception)) {
                throw new DatasetObjectStorageCredentialsException("Invalid credentials for storage object: " + key.value());
            }

            if (isNotFound(exception)) {
                throw new DatasetObjectNotFoundException("Dataset object not found: " + key.value());
            }

            throw new DatasetObjectStorageException("Cannot read dataset object metadata: " + key.value(), exception);
        }
    }

    @Override
    public InputStream openStream(DatasetObjectKey key) {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(properties.bucketName())
                    .key(storageKey(key))
                    .build());
        } catch (AwsServiceException exception) {
            if (isUnauthorized(exception)) {
                throw new DatasetObjectStorageCredentialsException("Invalid credentials for storage object: " + key.value());
            }

            if (isNotFound(exception)) {
                throw new DatasetObjectNotFoundException("Dataset object not found: " + key.value());
            }

            throw new DatasetObjectStorageException("Cannot open dataset object stream: " + key.value(), exception);
        }
    }

    private HeadObjectRequest headObjectRequest(DatasetObjectKey key) {
        return HeadObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(storageKey(key))
                .build();
    }

    private String storageKey(DatasetObjectKey key) {
        String prefix = properties.csvPrefix();

        if (prefix.isBlank()) {
            return key.value();
        }

        return prefix + "/" + key.value();
    }

    private boolean isNotFound(AwsServiceException exception) {
        return exception.statusCode() == NOT_FOUND_STATUS;
    }

    private boolean isUnauthorized(AwsServiceException exception) {
        return exception.statusCode() == UNAUTHORIZED_STATUS || exception.statusCode() == FORBIDDEN_STATUS;
    }
}
