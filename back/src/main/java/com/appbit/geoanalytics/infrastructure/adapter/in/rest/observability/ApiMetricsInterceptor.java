package com.appbit.geoanalytics.infrastructure.adapter.in.rest.observability;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ApiMetricsInterceptor implements HandlerInterceptor {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern UUID_NO_DASH_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{32}");

    private final MeterRegistry meterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        var startTime = (Long) request.getAttribute("startTime");
        if (startTime == null) return;

        var duration = System.currentTimeMillis() - startTime;
        var method = request.getMethod();
        var path = normalizePath(request.getRequestURI());
        var status = response.getStatus();

        meterRegistry.counter("api.requests.total",
                "method", method, "path", path, "status", String.valueOf(status)
        ).increment();

        meterRegistry.timer("api.requests.duration",
                "method", method, "path", path
        ).record(java.time.Duration.ofMillis(duration));

        if (ex != null) {
            meterRegistry.counter("api.requests.errors",
                    "method", method, "path", path, "exception", ex.getClass().getSimpleName()
            ).increment();
        }
    }

    /**
     * Normalize path by replacing UUIDs and numeric IDs with placeholders
     * to prevent Prometheus metric cardinality explosion.
     */
    private static String normalizePath(String uri) {
        var normalized = UUID_PATTERN.matcher(uri).replaceAll("{id}");
        normalized = UUID_NO_DASH_PATTERN.matcher(normalized).replaceAll("{id}");
        return normalized;
    }
}
