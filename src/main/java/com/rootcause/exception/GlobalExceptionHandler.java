package com.rootcause.exception;

import com.rootcause.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API errors.
 *
 * <p>This component centralizes exception-to-response translation for the RootCause API.
 * It ensures that application errors are returned using a consistent {@link ApiErrorResponse}
 * structure instead of exposing raw framework exceptions to API consumers.</p>
 *
 * <p>The handler currently covers:</p>
 * <ul>
 *     <li>validation errors for invalid request payloads</li>
 *     <li>malformed or missing request bodies</li>
 *     <li>illegal argument errors raised by the application</li>
 *     <li>analysis-not-found cases</li>
 *     <li>unexpected unhandled exceptions</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean validation errors raised when a request body does not satisfy
     * the declared validation constraints.
     *
     * <p>All field validation errors are collected and merged into a single message
     * using the format {@code fieldName: validationMessage}.</p>
     *
     * @param exception validation exception raised by Spring MVC
     * @param request current HTTP request
     * @return standardized {@link ApiErrorResponse} with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request
    ) {
        final String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    /**
     * Handles cases where the request body is missing or cannot be parsed correctly.
     *
     * @param exception exception raised when the HTTP message cannot be read
     * @param request current HTTP request
     * @return standardized {@link ApiErrorResponse} with HTTP 400 status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException exception,
            final HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or malformed",
                request.getRequestURI()
        );
    }

    /**
     * Handles application-level illegal argument errors.
     *
     * <p>This is typically used for invalid inputs detected inside the service layer
     * after the request has already passed HTTP-level deserialization.</p>
     *
     * @param exception application exception containing the validation message
     * @param request current HTTP request
     * @return standardized {@link ApiErrorResponse} with HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            final IllegalArgumentException exception,
            final HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handles cases where a requested analysis does not exist.
     *
     * @param exception domain exception indicating that the analysis was not found
     * @param request current HTTP request
     * @return standardized {@link ApiErrorResponse} with HTTP 404 status
     */
    @ExceptionHandler(AnalysisNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAnalysisNotFoundException(
            final AnalysisNotFoundException exception,
            final HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    /**
     * Handles any unexpected exception not covered by more specific handlers.
     *
     * <p>This prevents uncaught internal exceptions from leaking implementation details
     * to API consumers.</p>
     *
     * @param exception unexpected exception raised during request processing
     * @param request current HTTP request
     * @return standardized {@link ApiErrorResponse} with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            final Exception exception,
            final HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected internal server error",
                request.getRequestURI()
        );
    }

    /**
     * Formats a single validation field error into a client-friendly message.
     *
     * @param fieldError validation field error returned by Spring
     * @return formatted error message using the pattern {@code field: message}
     */
    private String formatFieldError(final FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    /**
     * Builds a standardized error response entity for the provided HTTP status and message.
     *
     * @param status HTTP status to return
     * @param message application-level error message
     * @param path request path that triggered the error
     * @return response entity containing the structured API error payload
     */
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            final HttpStatus status,
            final String message,
            final String path
    ) {
        final ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity.status(status).body(response);
    }
}