package com.appbit.geoanalytics.infrastructure.adapter.out.storage.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.net.URISyntaxException;

@ConfigurationProperties(prefix = "appbit.storage.r2")
@Validated
public record R2StorageProperties(
        @NotBlank(message = "appbit.storage.r2.endpoint must not be blank")
        String endpoint,
        @NotBlank(message = "appbit.storage.r2.access-key-id must not be blank")
        String accessKeyId,
        @NotBlank(message = "appbit.storage.r2.secret-access-key must not be blank")
        String secretAccessKey,
        @NotBlank(message = "appbit.storage.r2.bucket-name must not be blank")
        String bucketName,
        @NotBlank(message = "appbit.storage.r2.region must not be blank")
        String region,
        @NotBlank(message = "appbit.storage.r2.csv-prefix must not be blank")
        String csvPrefix
) {

    public R2StorageProperties {
        endpoint = strip(endpoint);
        accessKeyId = strip(accessKeyId);
        secretAccessKey = strip(secretAccessKey);
        bucketName = strip(bucketName);
        region = strip(region);
        csvPrefix = normalizePrefix(csvPrefix);

        validateEndpoint(endpoint);
        validateBucketName(bucketName);
    }

    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    private static String normalizePrefix(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.strip().replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private static void validateEndpoint(String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException exception) {
            throw new R2StoragePropertiesException("appbit.storage.r2.endpoint must be a valid URI", exception);
        }

        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new R2StoragePropertiesException("appbit.storage.r2.endpoint must use http or https");
        }

        if (uri.getHost() == null) {
            throw new R2StoragePropertiesException("appbit.storage.r2.endpoint must include a host");
        }
    }

    private static void validateBucketName(String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (value.contains("/") || value.contains("\\")) {
            throw new R2StoragePropertiesException("appbit.storage.r2.bucket-name must not contain path separators");
        }
    }
}
