package com.rootcause.dto;

import java.time.OffsetDateTime;

/**
 * Public API response used to represent an error returned by the application.
 *
 * <p>This DTO provides a consistent structure for HTTP error responses exposed by
 * the API. It is intended to help clients understand both the HTTP-level failure
 * and the application-specific message associated with it.</p>
 *
 * <p>The response includes the timestamp of the error, the HTTP status code,
 * the standard status label, a descriptive message, and the request path
 * that produced the error.</p>
 *
 * @param timestamp moment when the error response was generated
 * @param status HTTP status code returned by the API
 * @param error standard HTTP error label associated with the status code
 * @param message application-level error message
 * @param path request path that triggered the error
 */
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}