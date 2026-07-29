package com.appbit.geoanalytics.infrastructure.adapter.out.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;

@Component
@Getter
@Slf4j
public class MinioConnectivity {

    private final String endpoint;
    private final boolean available;

    public MinioConnectivity(@Value("${appbit.storage.r2.endpoint:}") String endpoint) {
        this.endpoint = endpoint;
        this.available = checkAvailability(endpoint);
    }

    public boolean isAvailable() {
        return available;
    }

    private boolean checkAvailability(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return false;
        }
        try {
            URLConnection connection = new URL(endpoint).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            return true;
        } catch (Exception e) {
            log.debug("MinIO not available at {}: {}", endpoint, e.getMessage());
            return false;
        }
    }
}
