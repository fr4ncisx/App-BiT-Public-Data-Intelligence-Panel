package com.appbit.geoanalytics.application.ai;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void acceptsValidQuery() {
        var request = new QueryRequest("¿Cuál es la brecha de formación en Florianópolis?", null, null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsBlankQuery() {
        var request = new QueryRequest("   ", null, null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("query");
    }

    @Test
    void rejectsNullQuery() {
        var request = new QueryRequest(null, null, null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("query");
    }

    @Test
    void rejectsQueryOver500Chars() {
        String longQuery = "x".repeat(501);

        var request = new QueryRequest(longQuery, null, null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("query");
    }

    @Test
    void acceptsNullOptionals() {
        var request = new QueryRequest("consulta válida", null, null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void acceptsAllFields() {
        var request = new QueryRequest(
                "consulta válida", "REG_FLORIANOPOLIS", "TRAINING", "MONTHLY", "ES"
        );

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsRegionCodeOver80Chars() {
        var request = new QueryRequest("consulta válida", "x".repeat(81), null, null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("regionCode");
    }

    @Test
    void rejectsIndicatorTypeOver80Chars() {
        var request = new QueryRequest("consulta válida", null, "x".repeat(81), null, null);

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("indicatorType");
    }

    @Test
    void rejectsLanguageOver16Chars() {
        var request = new QueryRequest("consulta válida", null, null, null, "x".repeat(17));

        Set<ConstraintViolation<QueryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("language");
    }
}
