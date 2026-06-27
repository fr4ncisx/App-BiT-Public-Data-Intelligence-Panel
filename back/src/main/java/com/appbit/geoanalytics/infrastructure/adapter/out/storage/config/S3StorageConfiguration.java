package com.appbit.geoanalytics.infrastructure.adapter.out.storage.config;

import com.appbit.geoanalytics.infrastructure.adapter.out.storage.properties.R2StorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class S3StorageConfiguration {

    private static final Duration API_CALL_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public S3Client datasetS3Client(R2StorageProperties properties) {
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        ClientOverrideConfiguration overrideConfiguration = ClientOverrideConfiguration.builder()
                .apiCallTimeout(API_CALL_TIMEOUT)
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        properties.accessKeyId(),
                        properties.secretAccessKey()
                )))
                .serviceConfiguration(serviceConfiguration)
                .overrideConfiguration(overrideConfiguration)
                .build();
    }
}
