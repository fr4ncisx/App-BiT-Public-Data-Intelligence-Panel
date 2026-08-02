package com.appbit.geoanalytics.infrastructure.adapter.in.rest.advice;

import com.appbit.geoanalytics.application.exception.ApplicationException;
import com.appbit.geoanalytics.domain.exception.DomainException;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
        var responseFactory = new ApiResponseFactory(clock);
        var exceptionHandler = new GlobalExceptionHandler(responseFactory, requestContext);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(exceptionHandler)
                .build();

        when(requestContext.requestId()).thenReturn("test-req-id");
    }

    @Test
    void handlesMethodArgumentNotValid() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].field").value("field"))
                .andExpect(jsonPath("$.errors[0].reason").value("must not be blank"))
                .andExpect(jsonPath("$.meta.requestId").value("test-req-id"));
    }

    @Test
    void handlesConstraintViolation() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void handlesMissingServletRequestParameter() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].field").value("q"))
                .andExpect(jsonPath("$.errors[0].reason").value("Required query parameter is missing."));
    }

    @Test
    void handlesMissingRequestHeader() throws Exception {
        mockMvc.perform(get("/test/missing-header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].field").value("X-Request-Id"))
                .andExpect(jsonPath("$.errors[0].reason").value("Required HTTP header is missing."));
    }

    @Test
    void handlesMissingPathVariable() throws Exception {
        mockMvc.perform(get("/test/missing-path"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.INTERNAL_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].field").value("id"))
                .andExpect(jsonPath("$.errors[0].reason")
                        .value("Required path variable is missing from the controller mapping."));
    }

    @Test
    void handlesHttpMessageNotReadable() throws Exception {
        mockMvc.perform(get("/test/not-readable"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.MALFORMED_REQUEST.name()))
                .andExpect(jsonPath("$.errors[0].field").value("body"))
                .andExpect(jsonPath("$.errors[0].reason").value("The JSON body is invalid, empty, or cannot be deserialized."));
    }

    @Test
    void handlesMethodArgumentTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/type-mismatch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].field").value("limit"))
                .andExpect(jsonPath("$.errors[0].reason").value("Expected data type 'Integer'."))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("abc"));
    }

    @Test
    void handlesNoResourceFoundWithNormalizedPath() throws Exception {
        mockMvc.perform(get("/test/no-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].field").value("path"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("/x/y"));
    }

    @Test
    void handlesNoResourceFoundWithBlankPath() throws Exception {
        mockMvc.perform(get("/test/no-resource-blank"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("/"));
    }

    @Test
    void handlesHttpRequestMethodNotSupported() throws Exception {
        mockMvc.perform(get("/test/method-not-supported"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.METHOD_NOT_ALLOWED.name()))
                .andExpect(jsonPath("$.errors[0].field").value("method"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("POST"));
    }

    @Test
    void handlesHttpMediaTypeNotSupported() throws Exception {
        mockMvc.perform(get("/test/media-type"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.UNSUPPORTED_MEDIA_TYPE.name()))
                .andExpect(jsonPath("$.errors[0].field").value("Content-Type"));
    }

    @Test
    void handlesDomainException() throws Exception {
        mockMvc.perform(get("/test/domain"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("Domain rule violated"));
    }

    @Test
    void handlesDomainExceptionWithBlankMessageUsingFallback() throws Exception {
        mockMvc.perform(get("/test/domain-blank"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("The request violates a domain rule."));
    }

    @Test
    void handlesApplicationException() throws Exception {
        mockMvc.perform(get("/test/application"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.AI_SERVICE_UNAVAILABLE.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("AI service down"));
    }

    @Test
    void handlesDataIntegrityViolation() throws Exception {
        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.DATA_CONFLICT.name()))
                .andExpect(jsonPath("$.errors[0].field").value("persistence"));
    }

    @Test
    void handlesIllegalArgumentAsBadRequest() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("Invalid indicator type: FOO"));
    }

    @Test
    void handlesIllegalArgumentWithNotFoundMessageAs404() throws Exception {
        mockMvc.perform(get("/test/illegal-argument-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("Region not found: FOO"));
    }

    @Test
    void handlesUncaughtExceptions() throws Exception {
        mockMvc.perform(get("/test/uncaught"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.INTERNAL_ERROR.name()))
                .andExpect(jsonPath("$.errors[0].reason").value("An unexpected condition occurred on the server."));
    }

    private static MethodParameter parameter() {
        try {
            Method method = ProbeController.class.getDeclaredMethod("dummyHandler", String.class);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @RestController
    static class ProbeController {

        @PostMapping("/test/valid")
        void valid(@Valid @RequestBody ProbeBody body) {
        }

        @GetMapping("/test/constraint-violation")
        void constraintViolation() {
            throw new ConstraintViolationException("Invalid", Set.of());
        }

        @GetMapping("/test/missing-param")
        void missingParam() throws Exception {
            throw new MissingServletRequestParameterException("q", "String");
        }

        @GetMapping("/test/missing-header")
        void missingHeader() throws Exception {
            throw new MissingRequestHeaderException("X-Request-Id", parameter());
        }

        @GetMapping("/test/missing-path")
        void missingPath() throws Exception {
            throw new MissingPathVariableException("id", parameter());
        }

        @GetMapping("/test/not-readable")
        void notReadable() {
            throw new HttpMessageNotReadableException("malformed", new HttpInputMessage() {
                @Override
                public HttpHeaders getHeaders() {
                    return new HttpHeaders();
                }

                @Override
                public InputStream getBody() {
                    return InputStream.nullInputStream();
                }
            });
        }

        @GetMapping("/test/type-mismatch")
        void typeMismatch() {
            throw new MethodArgumentTypeMismatchException("abc", Integer.class, "limit", null, null);
        }

        @GetMapping("/test/no-resource")
        void noResource() throws Exception {
            throw new NoResourceFoundException(HttpMethod.GET, "/", "x/y");
        }

        @GetMapping("/test/no-resource-blank")
        void noResourceBlank() throws Exception {
            throw new NoResourceFoundException(HttpMethod.GET, "/", "");
        }

        @GetMapping("/test/method-not-supported")
        void methodNotSupported() throws Exception {
            throw new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
        }

        @GetMapping("/test/media-type")
        void mediaType() throws Exception {
            throw new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, List.of(MediaType.TEXT_PLAIN));
        }

        @GetMapping("/test/domain")
        void domain() {
            throw new DomainException("Domain rule violated") {
            };
        }

        @GetMapping("/test/domain-blank")
        void domainBlank() {
            throw new DomainException("  ") {
            };
        }

        @GetMapping("/test/application")
        void application() {
            throw new ApplicationException("AI service down");
        }

        @GetMapping("/test/data-integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @GetMapping("/test/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("Invalid indicator type: FOO");
        }

        @GetMapping("/test/illegal-argument-not-found")
        void illegalArgumentNotFound() {
            throw new IllegalArgumentException("Region not found: FOO");
        }

        @GetMapping("/test/uncaught")
        void uncaught() throws Exception {
            throw new IllegalStateException("boom");
        }

        private static void dummyHandler(String value) {
        }

        static class ProbeBody {
            @NotBlank
            private String field;
        }
    }
}
