package com.appbit.geoanalytics.infrastructure.adapter.out.storage;

import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectMetadata;
import com.appbit.geoanalytics.application.storage.exception.DatasetObjectNotFoundException;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class S3DatasetObjectStorageIntegrationTest {

    @Autowired
    private MinioConnectivity minioConnectivity;

    private boolean minioAvailable;

    @BeforeAll
    void checkMinio() {
        minioAvailable = minioConnectivity.isAvailable();
    }

    private static final String TEST_BUCKET = "appbit-datasets";
    private static final String TEST_PREFIX = "raw/visent-cdrview";
    private static final String TEST_CSV_KEY = "integration-test-sample.csv";
    private static final String TEST_CSV_CONTENT = "id,name,value\n1,Test,100\n2,Sample,200\n";

    @Autowired
    private S3Client s3Client;

    @Autowired
    private DatasetObjectStoragePort storagePort;

    @Test
    @Order(1)
    void uploadsAndReadsCsvAsStream() throws Exception {
        Assumptions.assumeTrue(minioAvailable, "MinIO not available at " + minioConnectivity.getEndpoint());
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(TEST_BUCKET)
                    .build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {
        }

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(TEST_PREFIX + "/" + TEST_CSV_KEY)
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromBytes(TEST_CSV_CONTENT.getBytes(StandardCharsets.UTF_8)));

        DatasetObjectKey key = new DatasetObjectKey(TEST_CSV_KEY);

        assertThat(storagePort.exists(key)).isTrue();

        DatasetObjectMetadata metadata = storagePort.metadata(key);
        assertThat(metadata.key()).isEqualTo(key);
        assertThat(metadata.contentLength()).isGreaterThan(0);
        assertThat(metadata.contentType()).isEqualTo("text/csv");

        try (InputStream stream = storagePort.openStream(key)) {
            byte[] content = stream.readAllBytes();
            assertThat(new String(content, StandardCharsets.UTF_8)).isEqualTo(TEST_CSV_CONTENT);
        }
    }

    @Test
    @Order(2)
    void returnsFalseForNonExistentObject() {
        Assumptions.assumeTrue(minioAvailable, "MinIO not available at " + minioConnectivity.getEndpoint());
        DatasetObjectKey key = new DatasetObjectKey("non-existent-file.csv");

        assertThat(storagePort.exists(key)).isFalse();
    }

    @Test
    @Order(3)
    void throwsNotFoundExceptionWhenMetadataDoesNotExist() {
        Assumptions.assumeTrue(minioAvailable, "MinIO not available at " + minioConnectivity.getEndpoint());
        DatasetObjectKey key = new DatasetObjectKey("non-existent-file.csv");

        assertThatExceptionOfType(DatasetObjectNotFoundException.class)
                .isThrownBy(() -> storagePort.metadata(key));
    }

    @Test
    @Order(4)
    void throwsNotFoundExceptionWhenStreamDoesNotExist() {
        Assumptions.assumeTrue(minioAvailable, "MinIO not available at " + minioConnectivity.getEndpoint());
        DatasetObjectKey key = new DatasetObjectKey("non-existent-file.csv");

        assertThatExceptionOfType(DatasetObjectNotFoundException.class)
                .isThrownBy(() -> storagePort.openStream(key));
    }

    @Test
    @Order(5)
    void streamIsReadableWithoutLoadingAllInMemory() throws Exception {
        Assumptions.assumeTrue(minioAvailable, "MinIO not available at " + minioConnectivity.getEndpoint());
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(TEST_BUCKET)
                        .key(TEST_PREFIX + "/stream-test.csv")
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromBytes("a,b,c\n1,2,3\n".getBytes(StandardCharsets.UTF_8)));

        DatasetObjectKey key = new DatasetObjectKey("stream-test.csv");

        try (InputStream stream = storagePort.openStream(key)) {
            int firstByte = stream.read();
            assertThat(firstByte).isNotEqualTo(-1);
            assertThat((char) firstByte).isEqualTo('a');
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(TEST_BUCKET)
                .key(TEST_PREFIX + "/stream-test.csv")
                .build());
    }

    @AfterAll
    void cleanupBucket() {
        if (!minioAvailable) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(TEST_BUCKET)
                .key(TEST_PREFIX + "/" + TEST_CSV_KEY)
                .build());
    }
}
