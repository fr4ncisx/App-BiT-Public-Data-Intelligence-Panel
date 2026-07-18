package com.appbit.geoanalytics.infrastructure.adapter.in.rest.advice;

import com.appbit.geoanalytics.application.exception.ApplicationException;
import com.appbit.geoanalytics.domain.exception.DomainException;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.correlation.RequestContext;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.factory.ApiResponseFactory;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Stream;

/**
 * Centralized REST exception handler for the public HTTP API.
 *
 * <p>This class belongs to the REST input adapter. It translates Spring MVC,
 * validation, domain, persistence, and unexpected exceptions into the standardized
 * API response envelope used by the HTTP contract.</p>
 *
 * <p>It must not be used by domain or application services. Domain and application
 * layers should throw their own exceptions and remain independent of HTTP
 * concerns such as {@link ResponseEntity}, {@link HttpStatus} or response
 * serialization details.</p>
 *
 * <p>Response body creation is delegated to {@link ApiResponseFactory} to keep
 * metadata generation, API versioning, and response envelope construction in a
 * single REST infrastructure component.</p>
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ApiResponseFactory apiResponseFactory;
    private final RequestContext requestContext;

    /**
     * Handles validation errors raised when a request body annotated with
     * {@code @Valid} fails bean validation.
     *
     * <p>Both field-level errors and object-level errors are mapped into
     * structured {@link ApiResponse.ApiErrorDetail} entries.</p>
     *
     * @param ex validation exception raised by Spring MVC
     * @return standardized HTTP 400 response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Request body validation failed [{}]: {}", requestId, ex.getMessage());

        Stream<ApiResponse.ApiErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> detail(
                        error.getField(),
                        fallback(error.getDefaultMessage(), "Invalid field value."),
                        error.getRejectedValue()
                ));

        Stream<ApiResponse.ApiErrorDetail> globalErrors = ex.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(error -> detail(
                        error.getObjectName(),
                        fallback(error.getDefaultMessage(), "Invalid request body."),
                        null
                ));

        List<ApiResponse.ApiErrorDetail> errors = Stream.concat(fieldErrors, globalErrors)
                .toList();

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "The request contains invalid fields.",
                errors,
                requestId
        );
    }

    /**
     * Handles Jakarta Validation errors raised on request parameters, path
     * variables, or headers.
     *
     * <p>This usually applies when validation constraints are declared directly
     * on controller method parameters.</p>
     *
     * @param ex constraint violation exception raised by Jakarta Validation
     * @return standardized HTTP 400 response with parameter validation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Request parameter validation failed [{}]: {}", requestId, ex.getMessage());

        List<ApiResponse.ApiErrorDetail> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> detail(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()
                ))
                .toList();

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "The request contains invalid parameters.",
                errors,
                requestId
        );
    }

    /**
     * Handles requests that omit a required query parameter.
     *
     * @param ex exception raised when a required servlet request parameter is absent
     * @return standardized HTTP 400 response with missing parameter detail
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Missing request parameter [{}]: parameter='{}'", requestId, ex.getParameterName());

        ApiResponse.ApiErrorDetail error = detail(
                ex.getParameterName(),
                "Required query parameter is missing.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "Required query parameter is missing.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles requests that omit a required HTTP header.
     *
     * @param ex exception raised when a required request header is absent
     * @return standardized HTTP 400 response with missing header detail
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(
            MissingRequestHeaderException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Missing request header [{}]: header='{}'", requestId, ex.getHeaderName());

        ApiResponse.ApiErrorDetail error = detail(
                ex.getHeaderName(),
                "Required HTTP header is missing.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "Required HTTP header is missing.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles missing path variables in controller mappings.
     *
     * <p>This usually indicates an internal controller mapping configuration
     * problem rather than a client input error.</p>
     *
     * @param ex exception raised when a required path variable cannot be resolved
     * @return standardized HTTP 500 response with routing configuration detail
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(
            MissingPathVariableException ex
    ) {
        String requestId = requestContext.requestId();

        log.error(
                "Missing path variable in handler mapping [{}]: variable='{}'",
                requestId,
                ex.getVariableName()
        );

        ApiResponse.ApiErrorDetail error = detail(
                ex.getVariableName(),
                "Required path variable is missing from the controller mapping.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiResponseCode.INTERNAL_ERROR,
                "The request could not be processed due to an internal routing configuration error.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles unreadable, empty, or malformed HTTP request bodies.
     *
     * <p>This includes invalid JSON syntax, incompatible payload structures, and
     * request bodies that cannot be deserialized into the expected DTO.</p>
     *
     * @param ex exception raised when the request body cannot be read or converted
     * @return standardized HTTP 400 response with malformed request detail
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Malformed request body [{}]: {}", requestId, ex.getMessage());

        ApiResponse.ApiErrorDetail error = detail(
                "body",
                "The JSON body is invalid, empty, or cannot be deserialized.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.MALFORMED_REQUEST,
                "Request body is missing or malformed.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles type conversion failures in request parameters or path variables.
     *
     * <p>Examples include receiving non-numeric text for a numeric parameter or
     * an unsupported value for an enum-like controller argument.</p>
     *
     * @param ex exception raised when a request argument cannot be converted
     * @return standardized HTTP 400 response with type mismatch detail
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Request parameter type mismatch [{}]: {}", requestId, ex.getMessage());

        String requiredType = ex.getRequiredType() == null
                ? "unknown"
                : ex.getRequiredType().getSimpleName();

        ApiResponse.ApiErrorDetail error = detail(
                ex.getName(),
                "Expected data type '%s'.".formatted(requiredType),
                ex.getValue()
        );

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "Invalid parameter type provided.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles requests to missing static resources or unmapped endpoints.
     *
     * @param ex exception raised when no resource or endpoint is found
     * @return standardized HTTP 404 response with missing resource detail
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn(
                "Resource not found [{}]: path='{}'",
                requestId,
                ex.getResourcePath().isBlank() ? "/" : ex.getResourcePath()
        );

        ApiResponse.ApiErrorDetail error = detail(
                "path",
                "The requested endpoint or resource does not exist.",
                normalizedPath(ex.getResourcePath())
        );

        return apiResponseFactory.error(
                HttpStatus.NOT_FOUND,
                ApiResponseCode.RESOURCE_NOT_FOUND,
                "The requested resource could not be found.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles requests using an unsupported HTTP method for a matched endpoint.
     *
     * @param ex exception raised when the HTTP method is not supported
     * @return standardized HTTP 405 response with method detail
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("HTTP method not allowed [{}]: method='{}'", requestId, ex.getMethod());

        ApiResponse.ApiErrorDetail error = detail(
                "method",
                "HTTP method is not supported for this endpoint.",
                ex.getMethod()
        );

        return apiResponseFactory.error(
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiResponseCode.METHOD_NOT_ALLOWED,
                "HTTP method not allowed.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles requests with an unsupported {@code Content-Type}.
     *
     * @param ex exception raised when the request media type is not supported
     * @return standardized HTTP 415 response with media type detail
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex
    ) {
        String requestId = requestContext.requestId();

        log.warn("Unsupported media type [{}]: contentType='{}'", requestId, ex.getContentType());

        ApiResponse.ApiErrorDetail error = detail(
                "Content-Type",
                "Media type is not supported by this endpoint.",
                ex.getContentType()
        );

        return apiResponseFactory.error(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ApiResponseCode.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles domain rule violations raised by domain entities, value objects,
     * or domain services.
     *
     * <p>Domain exceptions represent controlled business rule violations. They
     * should not be exposed as internal server errors.</p>
     *
     * @param ex domain exception raised by the domain layer
     * @return standardized HTTP 400 response with domain validation detail
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        String requestId = requestContext.requestId();

        log.warn("Domain validation failed [{}]: {}", requestId, ex.getMessage());

        ApiResponse.ApiErrorDetail error = detail(
                null,
                fallback(ex.getMessage(), "The request violates a domain rule."),
                null
        );

        return apiResponseFactory.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseCode.VALIDATION_ERROR,
                "The request violates a domain rule.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles application layer exceptions such as service unavailability.
     *
     * <p>Application exceptions represent controlled failures in the application
     * layer, such as an external service (e.g. AI provider) being temporarily
     * unreachable. They are not domain rule violations and should not be exposed
     * as client errors.</p>
     *
     * @param ex application exception raised by an application service
     * @return standardized HTTP 503 response with service unavailability detail
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex) {
        String requestId = requestContext.requestId();

        log.warn("Application error [{}]: {}", requestId, ex.getMessage());

        ApiResponse.ApiErrorDetail error = detail(
                null,
                fallback(ex.getMessage(), "An application error occurred."),
                null
        );

        return apiResponseFactory.error(
                HttpStatus.SERVICE_UNAVAILABLE,
                ApiResponseCode.AI_SERVICE_UNAVAILABLE,
                "The AI service is temporarily unavailable.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles database integrity conflicts.
     *
     * <p>This method acts as a REST-level safety net for persistence constraint
     * violations such as duplicated unique keys or foreign key violations. In
     * stricter clean architecture implementations, persistence adapters can
     * translate these exceptions into application-specific exceptions before
     * they reach the REST adapter.</p>
     *
     * @param ex exception raised when persistence integrity is violated
     * @return standardized HTTP 409 response with conflict detail
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        String requestId = requestContext.requestId();

        log.error("Data integrity conflict [{}]", requestId, ex);

        ApiResponse.ApiErrorDetail error = detail(
                "persistence",
                "The operation conflicts with an existing persistence constraint.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.CONFLICT,
                ApiResponseCode.DATA_CONFLICT,
                "A data integrity conflict occurred.",
                List.of(error),
                requestId
        );
    }

    /**
     * Handles any uncaught exception not covered by more specific handlers.
     *
     * <p>This is the final safety net for unexpected failures. It avoids leaking
     * internal exception details to API clients while preserving diagnostic
     * information in application logs.</p>
     *
     * @param ex uncaught exception raised during request processing
     * @return standardized HTTP 500 response with generic internal error detail
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtExceptions(
            Exception ex
    ) {
        String requestId = requestContext.requestId();

        log.error("Unexpected internal error [{}]", requestId, ex);

        ApiResponse.ApiErrorDetail error = detail(
                null,
                "An unexpected condition occurred on the server.",
                null
        );

        return apiResponseFactory.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiResponseCode.INTERNAL_ERROR,
                "The request could not be processed due to an internal server error.",
                List.of(error),
                requestId
        );
    }

    /**
     * Builds a structured API error detail.
     *
     * <p>The rejected value is converted to {@link String} because the public API
     * error contract exposes rejected values as nullable text. Blank fields and
     * rejected values are normalized to {@code null}.</p>
     *
     * @param field field, parameter, header, or request component associated with the error
     * @param reason human-readable reason explaining the error
     * @param rejectedValue rejected value received by the API, when available
     * @return structured API error detail
     */
    private ApiResponse.ApiErrorDetail detail(
            @Nullable String field,
            String reason,
            @Nullable Object rejectedValue
    ) {
        return new ApiResponse.ApiErrorDetail(
                normalizeNullable(field),
                fallback(reason, "Invalid request."),
                rejectedValue == null ? null : normalizeNullable(String.valueOf(rejectedValue))
        );
    }

    /**
     * Returns a normalized fallback value when the provided text is null or blank.
     *
     * @param value candidate text value
     * @param defaultValue fallback text returned when the candidate value is empty
     * @return stripped candidate value when present; otherwise, stripped fallback value
     */
    private String fallback(@Nullable String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue.strip();
        }

        return value.strip();
    }

    /**
     * Normalizes a request path for error reporting.
     *
     * @param path raw path reported by Spring MVC
     * @return stripped path value, using {@code /} when the path is missing or blank
     */
    private String normalizedPath(@Nullable String path) {
        if (path == null || path.isBlank())
            return "/";

        String normalized = path.strip();

        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    /**
     * Normalizes nullable text values used by the public error contract.
     *
     * @param value nullable text value
     * @return stripped value, or {@code null} when the value is null or blank
     */
    private @Nullable String normalizeNullable(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }
}