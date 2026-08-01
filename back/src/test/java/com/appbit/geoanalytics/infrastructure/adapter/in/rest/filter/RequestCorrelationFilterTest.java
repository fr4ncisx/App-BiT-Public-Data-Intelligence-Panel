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
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "abc-1234_xy");
        var response = new MockHttpServletResponse();

        org.mockito.Mockito.doAnswer(invocation -> {
            assertThat(MDC.get(RequestCorrelationConstants.REQUEST_ID_MDC_KEY)).isEqualTo("abc-1234_xy");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE)).isEqualTo("abc-1234_xy");
        assertThat(response.getHeader(RequestCorrelationConstants.REQUEST_ID_HEADER)).isEqualTo("abc-1234_xy");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void removesMdcAfterChainCompletes() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "abc-1234_xy");

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
        request.addHeader(RequestCorrelationConstants.REQUEST_ID_HEADER, "  abc-1234_xy  ");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(request.getAttribute(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE)).isEqualTo("abc-1234_xy");
    }
}
