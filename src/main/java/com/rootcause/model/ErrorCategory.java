package com.rootcause.model;

/**
 * Enumeration of the error categories supported by the RootCause analysis engine.
 *
 * <p>Each value represents a high-level diagnostic classification that can be assigned
 * when the system analyzes an error message, log excerpt, or stack trace.</p>
 *
 * <p>These categories are used internally by the rule engine and are later exposed
 * through the public API as string values.</p>
 */
public enum ErrorCategory {

    /**
     * Failures related to establishing a connection to a database.
     */
    DATABASE_CONNECTION,

    /**
     * Failures related to verifying the identity of the caller.
     */
    AUTHENTICATION,

    /**
     * Failures related to missing permissions or insufficient access rights.
     */
    AUTHORIZATION,

    /**
     * Failures caused by attempting to bind to a port that is already in use.
     */
    PORT_IN_USE,

    /**
     * Failures caused by missing environment variables or unresolved configuration placeholders.
     */
    MISSING_ENVIRONMENT_VARIABLE,

    /**
     * Failures caused by dereferencing a null reference.
     */
    NULL_POINTER,

    /**
     * Failures caused by invalid syntax in the input, source, or parsed content.
     */
    SYNTAX_ERROR,

    /**
     * Failures caused by operations exceeding the allowed execution time.
     */
    TIMEOUT,

    /**
     * Failures related to SQL execution, SQL grammar, or schema/database interaction issues.
     */
    SQL_ERROR,

    /**
     * Failures caused by accessing a file or path that does not exist.
     */
    FILE_NOT_FOUND,

    /**
     * Fallback category used when the input does not match any known rule strongly enough.
     */
    UNKNOWN
}