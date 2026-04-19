package com.rootcause.controller;

import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.exception.GlobalExceptionHandler;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.mapper.AnalysisStatsResponseMapper;
import com.rootcause.model.AnalysisCount;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.AnalysisStats;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import com.rootcause.service.AnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web MVC tests for {@link AnalysisController}.
 *
 * <p>These tests verify the public HTTP contract exposed by the analysis controller,
 * including successful requests, paginated history retrieval, filter handling,
 * statistics retrieval, and mapped error responses returned by the global exception
 * handler.</p>
 *
 * <p>This test suite is intentionally focused on the controller layer contract and
 * therefore mocks the application service.</p>
 */
@WebMvcTest(AnalysisController.class)
@Import({GlobalExceptionHandler.class, AnalysisResponseMapper.class, AnalysisStatsResponseMapper.class})
class AnalysisControllerTest {

    private static final OffsetDateTime ANALYZED_FROM = OffsetDateTime.parse("2026-04-13T00:00:00Z");
    private static final OffsetDateTime ANALYZED_TO = OffsetDateTime.parse("2026-04-19T23:59:59Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    /**
     * Verifies that the analysis endpoint returns a successful diagnosis response
     * when the input payload is valid.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("POST /api/v1/analyze should return analysis response")
    void shouldAnalyzeInputSuccessfully() throws Exception {
        final String inputText = "org.postgresql.util.PSQLException: Connection refused\nFailed to obtain JDBC Connection";

        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "The application cannot establish a connection to the database.",
                List.of("connection refused", "failed to obtain jdbc connection"),
                List.of("Verify database availability", "Check datasource configuration"),
                new BigDecimal("0.80"),
                "database-connection-rule",
                inputText.length(),
                1
        );

        when(analysisService.analyze(inputText)).thenReturn(result);

        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "org.postgresql.util.PSQLException: Connection refused\\nFailed to obtain JDBC Connection"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.ruleCode").value("database-connection-rule"))
                .andExpect(jsonPath("$.rawInputLength").value(inputText.length()))
                .andExpect(jsonPath("$.matchedRuleCount").value(1))
                .andExpect(jsonPath("$.confidence").value(0.80));
    }

    /**
     * Verifies that a stored analysis can be retrieved successfully by its identifier.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses/{id} should return stored analysis")
    void shouldGetAnalysisByIdSuccessfully() throws Exception {
        final UUID analysisId = UUID.randomUUID();

        final AnalysisResult result = new AnalysisResult(
                analysisId,
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.NULL_POINTER,
                Severity.HIGH,
                "A null value is being dereferenced.",
                List.of("nullpointerexception"),
                List.of("Check null guards"),
                new BigDecimal("0.72"),
                "null-pointer-rule",
                120,
                2
        );

        when(analysisService.getAnalysisById(analysisId)).thenReturn(result);

        mockMvc.perform(get("/api/v1/analyses/{id}", analysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(analysisId.toString()))
                .andExpect(jsonPath("$.category").value("NULL_POINTER"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.ruleCode").value("null-pointer-rule"))
                .andExpect(jsonPath("$.rawInputLength").value(120))
                .andExpect(jsonPath("$.matchedRuleCount").value(2))
                .andExpect(jsonPath("$.confidence").value(0.72));
    }

    /**
     * Verifies that the paginated history endpoint returns the expected summary page
     * when no optional filters are provided.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return paged analysis summaries")
    void shouldGetPagedAnalysesSuccessfully() throws Exception {
        final AnalysisResult first = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "Cannot connect to database.",
                List.of("connection refused"),
                List.of("Verify database availability"),
                new BigDecimal("0.80"),
                "database-connection-rule",
                100,
                1
        );

        final AnalysisResult second = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-12T08:00:00Z"),
                ErrorCategory.TIMEOUT,
                Severity.HIGH,
                "The operation exceeded the allowed execution time.",
                List.of("timeout"),
                List.of("Review timeout thresholds"),
                new BigDecimal("0.67"),
                "timeout-rule",
                95,
                2
        );

        final Page<AnalysisResult> resultPage = new PageImpl<>(
                List.of(first, second),
                PageRequest.of(0, 20),
                2
        );

        when(analysisService.getAnalyses(null, null, null, null, null, 0, 20)).thenReturn(resultPage);

        mockMvc.perform(get("/api/v1/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].ruleCode").value("database-connection-rule"))
                .andExpect(jsonPath("$.items[0].category").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$.items[1].ruleCode").value("timeout-rule"))
                .andExpect(jsonPath("$.items[1].category").value("TIMEOUT"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    /**
     * Verifies that the paginated history endpoint applies the standard optional filters
     * when category, severity, and rule code are provided.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should apply filters when provided")
    void shouldGetPagedAnalysesWithFiltersSuccessfully() throws Exception {
        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "Cannot connect to database.",
                List.of("connection refused"),
                List.of("Verify database availability"),
                new BigDecimal("0.80"),
                "database-connection-rule",
                100,
                1
        );

        final Page<AnalysisResult> resultPage = new PageImpl<>(
                List.of(result),
                PageRequest.of(0, 10),
                1
        );

        when(analysisService.getAnalyses(
                eq("DATABASE_CONNECTION"),
                eq("CRITICAL"),
                eq("database-connection-rule"),
                eq(null),
                eq(null),
                eq(0),
                eq(10)
        )).thenReturn(resultPage);

        mockMvc.perform(get("/api/v1/analyses")
                        .param("category", "DATABASE_CONNECTION")
                        .param("severity", "CRITICAL")
                        .param("ruleCode", "database-connection-rule")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].category").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$.items[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$.items[0].ruleCode").value("database-connection-rule"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * Verifies that the history endpoint accepts an inclusive temporal range and
     * delegates the parsed {@link OffsetDateTime} values to the application service.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should apply date range filters when provided")
    void shouldGetPagedAnalysesWithDateRangeSuccessfully() throws Exception {
        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T21:47:10.393436Z"),
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                "The input does not match any known rule strongly enough.",
                List.of("no strong rule match"),
                List.of("Provide more technical context"),
                new BigDecimal("0.15"),
                "unknown-fallback-rule",
                80,
                0
        );

        final Page<AnalysisResult> resultPage = new PageImpl<>(
                List.of(result),
                PageRequest.of(0, 20),
                1
        );

        when(analysisService.getAnalyses(
                eq(null),
                eq(null),
                eq(null),
                eq(ANALYZED_FROM),
                eq(ANALYZED_TO),
                eq(0),
                eq(20)
        )).thenReturn(resultPage);

        mockMvc.perform(get("/api/v1/analyses")
                        .param("analyzedFrom", "2026-04-13T00:00:00Z")
                        .param("analyzedTo", "2026-04-19T23:59:59Z")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].ruleCode").value("unknown-fallback-rule"))
                .andExpect(jsonPath("$.items[0].category").value("UNKNOWN"))
                .andExpect(jsonPath("$.items[0].severity").value("LOW"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * Verifies that the history endpoint can combine category, severity, rule code,
     * and temporal filters in the same request.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should apply filters and date range together")
    void shouldGetPagedAnalysesWithFiltersAndDateRangeSuccessfully() throws Exception {
        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T21:47:10.393436Z"),
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                "The input does not match any known rule strongly enough.",
                List.of("no strong rule match"),
                List.of("Provide more technical context"),
                new BigDecimal("0.15"),
                "unknown-fallback-rule",
                80,
                0
        );

        final Page<AnalysisResult> resultPage = new PageImpl<>(
                List.of(result),
                PageRequest.of(0, 20),
                1
        );

        when(analysisService.getAnalyses(
                eq("UNKNOWN"),
                eq("LOW"),
                eq("unknown-fallback-rule"),
                eq(ANALYZED_FROM),
                eq(ANALYZED_TO),
                eq(0),
                eq(20)
        )).thenReturn(resultPage);

        mockMvc.perform(get("/api/v1/analyses")
                        .param("category", "UNKNOWN")
                        .param("severity", "LOW")
                        .param("ruleCode", "unknown-fallback-rule")
                        .param("analyzedFrom", "2026-04-13T00:00:00Z")
                        .param("analyzedTo", "2026-04-19T23:59:59Z")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].category").value("UNKNOWN"))
                .andExpect(jsonPath("$.items[0].severity").value("LOW"))
                .andExpect(jsonPath("$.items[0].ruleCode").value("unknown-fallback-rule"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * Verifies that the statistics endpoint returns the expected aggregated counts.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses/stats should return aggregated statistics")
    void shouldGetAnalysisStatsSuccessfully() throws Exception {
        final AnalysisStats stats = new AnalysisStats(
                6L,
                List.of(
                        new AnalysisCount("DATABASE_CONNECTION", 3L),
                        new AnalysisCount("TIMEOUT", 2L),
                        new AnalysisCount("NULL_POINTER", 1L)
                ),
                List.of(
                        new AnalysisCount("CRITICAL", 3L),
                        new AnalysisCount("HIGH", 2L),
                        new AnalysisCount("MEDIUM", 1L)
                )
        );

        when(analysisService.getAnalysisStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/analyses/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalyses").value(6))
                .andExpect(jsonPath("$.byCategory[0].value").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$.byCategory[0].count").value(3))
                .andExpect(jsonPath("$.bySeverity[0].value").value("CRITICAL"))
                .andExpect(jsonPath("$.bySeverity[0].count").value(3));
    }

    /**
     * Verifies that the history endpoint returns a 400 error when the requested page
     * index is invalid.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return 400 when page is invalid")
    void shouldReturnBadRequestWhenPageIsInvalid() throws Exception {
        when(analysisService.getAnalyses(null, null, null, null, null, -1, 20))
                .thenThrow(new IllegalArgumentException("page must be greater than or equal to 0"));

        mockMvc.perform(get("/api/v1/analyses")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("page must be greater than or equal to 0"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses"));
    }

    /**
     * Verifies that the history endpoint returns a 400 error when the requested page
     * size exceeds the supported limit.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return 400 when size is invalid")
    void shouldReturnBadRequestWhenSizeIsInvalid() throws Exception {
        when(analysisService.getAnalyses(null, null, null, null, null, 0, 101))
                .thenThrow(new IllegalArgumentException("size must be less than or equal to 100"));

        mockMvc.perform(get("/api/v1/analyses")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("size must be less than or equal to 100"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses"));
    }

    /**
     * Verifies that the history endpoint returns a 400 error when the category filter
     * does not match any supported category value.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return 400 when category is invalid")
    void shouldReturnBadRequestWhenCategoryIsInvalid() throws Exception {
        when(analysisService.getAnalyses("NOT_A_REAL_CATEGORY", null, null, null, null, 0, 20))
                .thenThrow(new IllegalArgumentException(
                        "category must be one of: DATABASE_CONNECTION, AUTHENTICATION, AUTHORIZATION, PORT_IN_USE, MISSING_ENVIRONMENT_VARIABLE, NULL_POINTER, SYNTAX_ERROR, TIMEOUT, SQL_ERROR, FILE_NOT_FOUND, UNKNOWN"
                ));

        mockMvc.perform(get("/api/v1/analyses")
                        .param("category", "NOT_A_REAL_CATEGORY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("category must be one of: DATABASE_CONNECTION, AUTHENTICATION, AUTHORIZATION, PORT_IN_USE, MISSING_ENVIRONMENT_VARIABLE, NULL_POINTER, SYNTAX_ERROR, TIMEOUT, SQL_ERROR, FILE_NOT_FOUND, UNKNOWN"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses"));
    }

    /**
     * Verifies that the history endpoint returns a 400 error when the severity filter
     * does not match any supported severity value.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return 400 when severity is invalid")
    void shouldReturnBadRequestWhenSeverityIsInvalid() throws Exception {
        when(analysisService.getAnalyses(null, "NOT_A_REAL_SEVERITY", null, null, null, 0, 20))
                .thenThrow(new IllegalArgumentException(
                        "severity must be one of: LOW, MEDIUM, HIGH, CRITICAL"
                ));

        mockMvc.perform(get("/api/v1/analyses")
                        .param("severity", "NOT_A_REAL_SEVERITY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("severity must be one of: LOW, MEDIUM, HIGH, CRITICAL"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses"));
    }

    /**
     * Verifies that the history endpoint returns a 400 error when the provided temporal
     * range is semantically invalid.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses should return 400 when analyzedFrom is after analyzedTo")
    void shouldReturnBadRequestWhenDateRangeIsInvalid() throws Exception {
        when(analysisService.getAnalyses(
                null,
                null,
                null,
                OffsetDateTime.parse("2026-04-20T00:00:00Z"),
                OffsetDateTime.parse("2026-04-19T00:00:00Z"),
                0,
                20
        )).thenThrow(new IllegalArgumentException(
                "analyzedFrom must be less than or equal to analyzedTo"
        ));

        mockMvc.perform(get("/api/v1/analyses")
                        .param("analyzedFrom", "2026-04-20T00:00:00Z")
                        .param("analyzedTo", "2026-04-19T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("analyzedFrom must be less than or equal to analyzedTo"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses"));
    }

    /**
     * Verifies that the not-found error emitted by the application service is correctly
     * translated into the public 404 API response.
     *
     * @throws Exception when the MVC request execution fails unexpectedly
     */
    @Test
    @DisplayName("GET /api/v1/analyses/{id} should return 404 when analysis does not exist")
    void shouldReturnNotFoundWhenAnalysisDoesNotExist() throws Exception {
        final UUID analysisId = UUID.randomUUID();

        when(analysisService.getAnalysisById(analysisId))
                .thenThrow(new AnalysisNotFoundException(analysisId));

        mockMvc.perform(get("/api/v1/analyses/{id}", analysisId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Analysis " + analysisId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses/" + analysisId));
    }
}