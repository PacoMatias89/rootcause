# RootCause

RootCause is a Java Spring Boot application that analyzes raw error messages, logs, and stack traces to produce a structured diagnostic result.

The project is being built as a real backend MVP with persistence from the beginning, using a rule-based engine as the initial analysis approach.

## Main Goal

The application receives a text input containing an error, log excerpt, or stack trace and returns a useful diagnosis including:

- error category
- severity
- probable cause
- detected patterns
- recommended next steps
- confidence score

The system is also prepared for persistence and future evolution.

---

## Current MVP Scope

The current version of RootCause can:

1. receive a raw technical error text
2. normalize and analyze the input
3. evaluate the input against rule-based patterns
4. select the best matching rule
5. persist the generated analysis
6. expose saved analyses through REST endpoints

---

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit 5
- Mockito

---

## Architectural Decisions

- Single Spring Boot application
- No multi-module structure yet
- Rule-based engine for the MVP
- No real AI at this stage
- Persistence enabled from the start
- Clear separation by package responsibility

---

## Known Packages

```text
com.rootcause
- controller
- dto
- service
- rules
- model
- repository
- entity
- mapper
- config
- exception
- util

```
---
## API Endpoints
1. Analyze input

>POST /api/v1/analyze

Analyzes an input text and stores the result.

#### Example request

```json
{
"inputText": "org.postgresql.util.PSQLException: Connection refused\nFailed to obtain JDBC Connection"
}
```

#### Example response
```json
{
"analysisId": "2c8c6d36-0d4c-4a8d-a3f5-955b9b11f8e1",
"analyzedAt": "2026-04-13T21:15:30.123456Z",
"category": "DATABASE_CONNECTION",
"severity": "CRITICAL",
"probableCause": "The application cannot establish a connection to the database.",
"detectedPatterns": [
"connection refused",
"failed to obtain jdbc connection"
],
"recommendedSteps": [
"Verify that the database host and port are reachable.",
"Check whether the database service is running.",
"Validate datasource configuration and credentials."
],
"confidence": 0.80
}
```
### 2. Get one saved analysis

>GET /api/v1/analyses/{id}

Returns a saved analysis by its identifier.

### 3. Get saved analyses

>GET /api/v1/analyses

Returns the stored analyses history.

---
## Validation Rules

The analyze request uses the following constraints on inputText:

must not be blank
maximum length: 20000 characters

Example validation error:
```json
{
"timestamp": "2026-04-13T23:50:49.2700969+02:00",
"status": 400,
"error": "Bad Request",
"message": "inputText: inputText must not be blank",
"path": "/api/v1/analyze"
}
```
---
### Supported Categories
```text
DATABASE_CONNECTION
AUTHENTICATION
AUTHORIZATION
PORT_IN_USE
MISSING_ENVIRONMENT_VARIABLE
NULL_POINTER
SYNTAX_ERROR
TIMEOUT
SQL_ERROR
FILE_NOT_FOUND
UNKNOWN
Supported Severities
LOW
MEDIUM
HIGH
CRITICAL
Persistence
```

The project uses PostgreSQL for persistence and Flyway for schema migrations.

#### Existing base migration

>V1__create_analysis_record.sql

Creates the analysis_record table and the main indexes used for retrieval by category and analysis date.

---
### Current Configuration

The application uses an application.yml file with environment-variable support for the datasource:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
SERVER_PORT
```


Default local values are already defined in the configuration.

---

### Running the Project Locally
#### 1. Requirements

    Make sure you have:

    * Java 21
    * Maven
    * PostgreSQL running locally or accessible remotely
#### 2. Configure the database

You can rely on the defaults from application.yml or set environment variables:

```env
export DB_HOST=YOU_PORT
export DB_PORT=YOU_DB_PORT
export DB_NAME=YOU_DB_NAME
export DB_USER=YOU_DB_USER
export DB_PASSWORD=YOU_DB_PASSWORD
export SERVER_PORT=YOU_SERVER_PORT
```

#### 3. Start the application

```bash
mvn spring-boot:run
```
##### Running Tests
```bash
mvn test
```

### Current Functional Status
The application is already working end to end.

Validated manually:

* POST /api/v1/analyze
* GET /api/v1/analyses/{id}
* GET /api/v1/analyses
* 400 Bad Request for invalid input
* 404 Not found for unknown analysis id

Persistence is active and the application has been tested successfully with Postman.