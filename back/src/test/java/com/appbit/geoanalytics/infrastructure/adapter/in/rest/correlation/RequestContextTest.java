package com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextTest {

    private final RequestContext requestContext = new RequestContext();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void returnsRequestIdFromAttribute() {
        var request = new MockHttpServletRequest();
        request.setAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE, "req-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(requestContext.requestId()).isEqualTo("req-123");
    }

    @Test
    void stripsRequestIdFromAttribute() {
        var request = new MockHttpServletRequest();
        request.setAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE, "  req-123  ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThat(requestContext.requestId()).isEqualTo("req-123");
    }

    @Test
    void generatesUuidWhenNoRequestContext() {
        String requestId = requestContext.requestId();

        assertThat(requestId).isEqualTo(UUID.fromString(requestId).toString());
    }

    @Test
    void generatesUuidWhenAttributeIsNotString() {
        var request = new MockHttpServletRequest();
        request.setAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE, 42);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String requestId = requestContext.requestId();

        assertThat(requestId).isEqualTo(UUID.fromString(requestId).toString());
    }

    @Test
    void generatesUuidWhenAttributeIsBlank() {
        var request = new MockHttpServletRequest();
        request.setAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE, "   ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String requestId = requestContext.requestId();

        assertThat(requestId).isEqualTo(UUID.fromString(requestId).toString());
    }
}
