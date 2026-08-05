package com.appbit.geoanalytics.application.ingestion.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    void acceptsValidConfiguration() {
        var policy = new RetryPolicy(3, 2000);

        assertThat(policy.maxAttempts()).isEqualTo(3);
        assertThat(policy.baseDelayMillis()).isEqualTo(2000);
    }

    @Test
    void acceptsSingleAttemptWithNoDelay() {
        var policy = new RetryPolicy(1, 0);

        assertThat(policy.maxAttempts()).isEqualTo(1);
        assertThat(policy.baseDelayMillis()).isZero();
    }

    @Test
    void rejectsZeroMaxAttempts() {
        assertThatThrownBy(() -> new RetryPolicy(0, 2000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeMaxAttempts() {
        assertThatThrownBy(() -> new RetryPolicy(-1, 2000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeBaseDelay() {
        assertThatThrownBy(() -> new RetryPolicy(3, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}