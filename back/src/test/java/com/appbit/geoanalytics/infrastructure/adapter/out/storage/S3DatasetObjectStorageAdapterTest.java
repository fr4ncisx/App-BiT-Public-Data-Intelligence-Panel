package com.appbit.geoanalytics.infrastructure.adapter.out.storage;

import com.appbit.geoanalytics.application.storage.exception.DatasetObjectNotFoundException;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectStorageCredentialsException;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectStorageException;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectMetadata;
import com.appbit.geoanalytics.infrastructure.adapter.out.storage.properties.R2StorageProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.storage.r2.S3DatasetObjectStorageAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3DatasetObjectStorageAdapterTest {

    private static final R2StorageProperties PROPERTIES = new R2StorageProperties(
            "http://localhost:9000",
            "access-key",
            "secret-key",
            "appbit-datasets",
            "auto",
            "raw/visent-cdrview"
    );

    private static final AwsServiceException NOT_FOUND_EXCEPTION = AwsServiceException.builder()
            .statusCode(404)
            .message("not found")
            .build();

    private static final AwsServiceException FORBIDDEN_EXCEPTION = AwsServiceException.builder()
            .statusCode(403)
            .message("forbidden")
            .build();

    private final S3Client s3Client = mock(S3Client.class);
    private final S3DatasetObjectStorageAdapter adapter = new S3DatasetObjectStorageAdapter(s3Client, PROPERTIES);

    @Test
    void checksObjectExistenceUsingPrefixedKey() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());

        boolean exists = adapter.exists(new DatasetObjectKey("antenas_flp.csv"));

        assertThat(exists).isTrue();
        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3Client).headObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("appbit-datasets");
        assertThat(captor.getValue().key()).isEqualTo("raw/visent-cdrview/antenas_flp.csv");
    }

    @Test
    void returnsFalseWhenObjectDoesNotExist() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NOT_FOUND_EXCEPTION);

        boolean exists = adapter.exists(new DatasetObjectKey("missing.csv"));

        assertThat(exists).isFalse();
    }

    @Test
    void mapsMetadata() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(42L)
                .eTag("etag-value")
                .contentType("text/csv")
                .build());

        DatasetObjectKey key = new DatasetObjectKey("antenas_flp.csv");
        DatasetObjectMetadata metadata = adapter.metadata(key);

        assertThat(metadata.key()).isEqualTo(key);
        assertThat(metadata.contentLength()).isEqualTo(42L);
        assertThat(metadata.eTag()).isEqualTo("etag-value");
        assertThat(metadata.contentType()).isEqualTo("text/csv");
    }

    @Test
    void opensObjectStreamWithoutMaterializingContent() throws Exception {
        GetObjectResponse response = GetObjectResponse.builder().contentLength(4L).build();
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream("data".getBytes()))
        );
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        try (InputStream result = adapter.openStream(new DatasetObjectKey("antenas_flp.csv"))) {
            assertThat(result.readAllBytes()).isEqualTo("data".getBytes());
        }

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("appbit-datasets");
        assertThat(captor.getValue().key()).isEqualTo("raw/visent-cdrview/antenas_flp.csv");
    }

    @Test
    void throwsNotFoundWhenMetadataObjectDoesNotExist() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NOT_FOUND_EXCEPTION);

        assertThatThrownBy(() -> adapter.metadata(new DatasetObjectKey("missing.csv")))
                .isInstanceOf(DatasetObjectNotFoundException.class)
                .hasMessage("Dataset object not found: missing.csv");
    }

    @Test
    void wrapsUnexpectedStorageErrors() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(AwsServiceException.builder()
                .statusCode(500)
                .message("storage error")
                .build());

        assertThatThrownBy(() -> adapter.metadata(new DatasetObjectKey("antenas_flp.csv")))
                .isInstanceOf(DatasetObjectStorageException.class)
                .hasMessage("Cannot read dataset object metadata: antenas_flp.csv");
    }

    @Test
    void throwsCredentialsExceptionOnExistsWhen403() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(FORBIDDEN_EXCEPTION);

        assertThatThrownBy(() -> adapter.exists(new DatasetObjectKey("antenas_flp.csv")))
                .isInstanceOf(DatasetObjectStorageCredentialsException.class)
                .hasMessage("Invalid credentials for storage object: antenas_flp.csv");
    }

    @Test
    void throwsCredentialsExceptionOnMetadataWhen403() {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(FORBIDDEN_EXCEPTION);

        assertThatThrownBy(() -> adapter.metadata(new DatasetObjectKey("antenas_flp.csv")))
                .isInstanceOf(DatasetObjectStorageCredentialsException.class)
                .hasMessage("Invalid credentials for storage object: antenas_flp.csv");
    }

    @Test
    void throwsCredentialsExceptionOnOpenStreamWhen403() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(FORBIDDEN_EXCEPTION);

        assertThatThrownBy(() -> adapter.openStream(new DatasetObjectKey("antenas_flp.csv")))
                .isInstanceOf(DatasetObjectStorageCredentialsException.class)
                .hasMessage("Invalid credentials for storage object: antenas_flp.csv");
    }

    @Test
    void throwsNotFoundOnOpenStreamWhen404() {
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(NOT_FOUND_EXCEPTION);

        assertThatThrownBy(() -> adapter.openStream(new DatasetObjectKey("missing.csv")))
                .isInstanceOf(DatasetObjectNotFoundException.class)
                .hasMessage("Dataset object not found: missing.csv");
    }
}
