package com.rootcause.model;

public enum ErrorCategory {
    DATABASE_CONNECTION,
    AUTHENTICATION,
    AUTHORIZATION,
    PORT_IN_USE,
    MISSING_ENVIRONMENT_VARIABLE,
    NULL_POINTER,
    SYNTAX_ERROR,
    TIMEOUT,
    SQL_ERROR,
    FILE_NOT_FOUND,
    UNKNOWN
}