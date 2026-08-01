package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ObservabilityConfigTest {

    @Mock
    private ApiMetricsInterceptor apiMetricsInterceptor;

    @Test
    void registersApiMetricsInterceptor() {
        var registry = new ExposedInterceptorRegistry();
        var config = new ObservabilityConfig(apiMetricsInterceptor);

        config.addInterceptors(registry);

        assertThat(registry.registered()).contains(apiMetricsInterceptor);
    }

    static class ExposedInterceptorRegistry extends InterceptorRegistry {
        List<Object> registered() {
            return getInterceptors();
        }
    }
}
