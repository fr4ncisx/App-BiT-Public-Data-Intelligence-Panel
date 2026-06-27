package com.appbit.geoanalytics.infrastructure.adapter.out.storage;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLConnection;

@Component
@Getter
public class MinioConnectivity {

    private final String endpoint;

    public MinioConnectivity(@Value("${appbit.storage.r2.endpoint}") String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isAvailable() {
        try {
            URLConnection connection = new URL(endpoint).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
