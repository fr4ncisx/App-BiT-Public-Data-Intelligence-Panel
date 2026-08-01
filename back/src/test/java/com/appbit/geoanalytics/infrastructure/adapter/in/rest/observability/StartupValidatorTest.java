package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StartupValidatorTest {

    @Mock private DataSource dataSource;
    @Mock private ApplicationContext applicationContext;
    @Mock private Connection connection;

    private StartupValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        when(applicationContext.getParent()).thenReturn(null);
        when(dataSource.getConnection()).thenReturn(connection);
        validator = new StartupValidator(dataSource, applicationContext);
    }

    @Test
    void skipsValidationInChildContext() throws Exception {
        when(applicationContext.getParent()).thenReturn(org.mockito.Mockito.mock(ApplicationContext.class));

        validator.run(new DefaultApplicationArguments());

        verifyNoInteractions(dataSource);
    }

    @Test
    void validatesConnectionSuccessfully() throws Exception {
        when(connection.isValid(5)).thenReturn(true);
        when(connection.getCatalog()).thenReturn("appbit");

        validator.run(new DefaultApplicationArguments());
    }

    @Test
    void throwsWhenConnectionIsInvalid() throws Exception {
        when(connection.isValid(5)).thenReturn(false);

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("invalid state");
    }

    @Test
    void throwsWhenConnectionCannotBeEstablished() throws Exception {
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection refused");
    }
}
