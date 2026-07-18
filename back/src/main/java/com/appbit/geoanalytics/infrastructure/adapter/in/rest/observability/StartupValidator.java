package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupValidator implements ApplicationRunner {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        if (applicationContext.getParent() != null) {
            return;
        }
        validateDatabaseConnection();
    }

    private void validateDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.info("StartupValidator — PostgreSQL connection OK: catalog={}", connection.getCatalog());
            } else {
                throw new IllegalStateException("StartupValidator — PostgreSQL connection returned invalid state");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("StartupValidator — PostgreSQL connection FAILED: " + e.getMessage(), e);
        }
    }
}
