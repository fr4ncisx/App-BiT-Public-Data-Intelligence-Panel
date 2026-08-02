package com.appbit.geoanalytics.infrastructure.adapter.in.rest.filter;

import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestCorrelationConstants;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestCorrelationFilterTest {

    private static final String VALID_UUID = "123e4567-e89b-12d3-a456-426614174000";

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void keepsValidRequestIdFromHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, VALID_UUID);
        var response = new MockHttpServletResponse();

        org.mockito.Mockito.doAnswer(invocation -> {
            assertThat(MDC.get(RequestCorrelationConstants.REQUEST_ID_MDC_KEY)).isEqualTo(VALID_UUID);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE)).isEqualTo(VALID_UUID);
        assertThat(response.getHeader(RequestCorrelationConstants.REQUEST_ID_HEADER)).isEqualTo(VALID_UUID);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void removesMdcAfterChainCompletes() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, VALID_UUID);

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(MDC.get(RequestCorrelationConstants.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void generatesNewIdWhenHeaderMissing() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isNotNull().isEqualTo(UUID.fromString(resolved).toString());
        assertThat(response.getHeader(RequestCorrelationConstants.REQUEST_ID_HEADER)).isEqualTo(resolved);
    }

    @Test
    void generatesNewIdWhenHeaderTooShort() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "abc");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void generatesNewIdWhenHeaderTooLong() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "x".repeat(65));

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void generatesNewIdWhenHeaderHasInvalidCharacters() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "abc def!");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void generatesNewIdWhenHeaderIsNotAUuid() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "abc-1234_xy");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void generatesNewIdWhenHeaderHasWrongUuidFormat() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "123e4567-e89b-12d3-a456-42661417400Z");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void generatesNewIdWhenHeaderIsBlank() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "   ");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        String resolved = (String) request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE);
        assertThat(resolved).isEqualTo(UUID.fromString(resolved).toString());
    }

    @Test
    void stripsWhitespaceFromValidHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "  " + VALID_UUID + "  ");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE)).isEqualTo(VALID_UUID);
    }

    @Test
    void acceptsUppercaseUuidFromHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, VALID_UUID.toUpperCase());

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE)).isEqualTo(VALID_UUID.toUpperCase());
    }
}
