package com.appbit.geoanalytics.infrastructure.adapter.in.rest.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@ConfigurationProperties(prefix = "appbit.api.cors")
@Validated
public record CorsProperties(
        @NotBlank(message = "appbit.api.cors.allowed-origins must not be blank")
        String allowedOrigins
) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? null : allowedOrigins.strip();

        if (allowedOrigins != null && !allowedOrigins.isBlank() && !isValidOrigin(allowedOrigins)) {
            throw new IllegalArgumentException("appbit.api.cors.allowed-origins must be a valid origin, for example: https://app.example.com");
        }
    }

    private static boolean isValidOrigin(String value) {
        try {
            URI uri = URI.create(value);

            return hasValidScheme(uri)
                    && uri.getHost() != null
                    && uri.getUserInfo() == null
                    && isBlank(uri.getPath())
                    && uri.getQuery() == null
                    && uri.getFragment() == null;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }

    private static boolean hasValidScheme(URI uri) {
        return "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
