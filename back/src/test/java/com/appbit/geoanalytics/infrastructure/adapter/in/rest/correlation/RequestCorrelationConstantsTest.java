package com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestCorrelationConstantsTest {

    @Test
    void exposesExpectedConstants() {
        assertThat(RequestCorrelationConstants.REQUEST_ID_HEADER).isEqualTo("X-Request-Id");
        assertThat(RequestCorrelationConstants.REQUEST_ID_ATTRIBUTE).isEqualTo("appbit.requestId");
        assertThat(RequestCorrelationConstants.REQUEST_ID_MDC_KEY).isEqualTo("requestId");
    }

    @Test
    void cannotBeInstantiated() throws Exception {
        Constructor<RequestCorrelationConstants> constructor =
                RequestCorrelationConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
                .hasCauseInstanceOf(AssertionError.class);
    }
}
