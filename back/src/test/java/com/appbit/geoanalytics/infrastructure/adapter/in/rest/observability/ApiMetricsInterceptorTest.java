package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMetricsInterceptorTest {

    private SimpleMeterRegistry meterRegistry;
    private ApiMetricsInterceptor interceptor;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        interceptor = new ApiMetricsInterceptor(meterRegistry);
    }

    private MockHttpServletRequest request(String uri) {
        var request = new MockHttpServletRequest("GET", uri);
        return request;
    }

    @Test
    void preHandleStoresStartTimeAndReturnsTrue() {
        var request = request("/api/v1/data/queries");

        boolean proceed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(proceed).isTrue();
        assertThat(request.getAttribute("startTime")).isInstanceOf(Long.class);
    }

    @Test
    void recordsTotalDurationAndTimer() {
        var request = request("/api/v1/data/queries");
        var response = new MockHttpServletResponse();
        response.setStatus(200);
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(meterRegistry.get("api.requests.total")
                .tag("method", "GET")
                .tag("path", "/api/v1/data/queries")
                .tag("status", "200")
                .counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("api.requests.duration")
                .tag("method", "GET")
                .tag("path", "/api/v1/data/queries")
                .timer().count()).isEqualTo(1L);
        assertThat(meterRegistry.get("api.requests.duration")
                .tag("method", "GET")
                .tag("path", "/api/v1/data/queries")
                .timer().totalTime(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void incrementsErrorCounterWhenExceptionPresent() {
        var request = request("/api/v1/data/queries");
        var response = new MockHttpServletResponse();
        response.setStatus(500);
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), new IllegalStateException("boom"));

        assertThat(meterRegistry.get("api.requests.errors")
                .tag("method", "GET")
                .tag("path", "/api/v1/data/queries")
                .tag("exception", "IllegalStateException")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void doesNothingWhenStartTimeMissing() {
        var request = request("/api/v1/data/queries");

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

        assertThat(meterRegistry.find("api.requests.total").counter()).isNull();
        assertThat(meterRegistry.find("api.requests.duration").timer()).isNull();
    }

    @Test
    void normalizesUuidInPath() {
        var request = request("/api/v1/data/regions/123e4567-e89b-42d3-a456-556642440000/indicators");
        var response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(meterRegistry.get("api.requests.total")
                .tag("path", "/api/v1/data/regions/{id}/indicators")
                .counter().count()).isEqualTo(1.0);
    }

    @Test
    void normalizesUuidWithoutDashesInPath() {
        var request = request("/api/v1/data/regions/123e4567e89b42d3a456556642440000");
        var response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, new Object());

        interceptor.afterCompletion(request, response, new Object(), null);

        assertThat(meterRegistry.get("api.requests.total")
                .tag("path", "/api/v1/data/regions/{id}")
                .counter().count()).isEqualTo(1.0);
    }
}
