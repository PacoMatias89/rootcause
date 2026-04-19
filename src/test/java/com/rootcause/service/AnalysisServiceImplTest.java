package com.rootcause.service;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.AnalysisDecision;
import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.AnalysisStats;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.repository.AnalysisGroupedCountProjection;
import com.rootcause.repository.AnalysisRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AnalysisServiceImpl}.
 *
 * <p>These tests verify the application service responsibilities such as input validation,
 * orchestration of the analysis flow, persistence interaction, historical retrieval,
 * statistics retrieval, pagination validation, semantic filter validation, and temporal
 * range validation.</p>
 */
class AnalysisServiceImplTest {

    private AnalysisEngine analysisEngine;
    private AnalysisRecordRepository analysisRecordRepository;
    private AnalysisRecordMapper analysisRecordMapper;
    private Clock clock;

    /**
     * Creates a fresh test fixture before each test execution.
     */
    @BeforeEach
    void setUp() {
        analysisEngine = mock(AnalysisEngine.class);
        analysisRecordRepository = mock(AnalysisRecordRepository.class);
        analysisRecordMapper = new AnalysisRecordMapper();
        clock = Clock.fixed(Instant.parse("2026-04-13T10:15:30Z"), ZoneOffset.UTC);
    }

    /**
     * Verifies that the analysis flow delegates to the engine, builds the domain result,
     * and persists the generated metadata.
     */
    @Test
    @DisplayName("Should analyze input and persist metadata from engine decision")
    void shouldAnalyzeInputAndPersistMetadataFromEngineDecision() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        final RuleMatch bestMatch = new RuleMatch(
                "database-connection-rule",
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                0.80,
                "The application cannot establish a connection to the database.",
                List.of("connection refused", "failed to obtain jdbc connection"),
                List.of("Verify database availability", "Check datasource configuration")
        );

        when(analysisEngine.analyze(any(AnalysisRequestContext.class)))
                .thenReturn(new AnalysisDecision(bestMatch, 2));

        when(analysisRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final String inputText =
                "org.postgresql.util.PSQLException: Connection refused\nFailed to obtain JDBC Connection";

        final AnalysisResult result = service.analyze(inputText);

        assertNotNull(result.analysisId());
        assertEquals(OffsetDateTime.parse("2026-04-13T10:15:30Z"), result.analyzedAt());
        assertEquals(ErrorCategory.DATABASE_CONNECTION, result.category());
        assertEquals(Severity.CRITICAL, result.severity());
        assertEquals("database-connection-rule", result.ruleCode());
        assertEquals(Integer.valueOf(inputText.trim().length()), result.rawInputLength());
        assertEquals(Integer.valueOf(2), result.matchedRuleCount());
        assertEquals(0, result.confidence().compareTo(new BigDecimal("0.80")));

        verify(analysisEngine, times(1)).analyze(any(AnalysisRequestContext.class));
        verify(analysisRecordRepository, times(1)).save(any());
    }

    /**
     * Verifies that an unknown diagnosis returned by the engine is also persisted correctly
     * as part of the historical analysis flow.
     */
    @Test
    @DisplayName("Should persist fallback result when engine returns unknown diagnosis")
    void shouldPersistFallbackResultWhenEngineReturnsUnknownDiagnosis() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        final RuleMatch fallbackMatch = new RuleMatch(
                "unknown-fallback-rule",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.15,
                "The input does not match any known rule strongly enough.",
                List.of("no strong rule match"),
                List.of("Provide more technical context")
        );

        when(analysisEngine.analyze(any(AnalysisRequestContext.class)))
                .thenReturn(new AnalysisDecision(fallbackMatch, 0));

        when(analysisRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final String inputText = "something unrecognized";
        final AnalysisResult result = service.analyze(inputText);

        assertEquals(ErrorCategory.UNKNOWN, result.category());
        assertEquals(Severity.LOW, result.severity());
        assertEquals("unknown-fallback-rule", result.ruleCode());
        assertEquals(Integer.valueOf(inputText.trim().length()), result.rawInputLength());
        assertEquals(Integer.valueOf(0), result.matchedRuleCount());
        assertEquals(0, result.confidence().compareTo(new BigDecimal("0.15")));

        verify(analysisEngine, times(1)).analyze(any(AnalysisRequestContext.class));
        verify(analysisRecordRepository, times(1)).save(any());
    }

    /**
     * Verifies that a stored analysis can be recovered by identifier when it exists.
     */
    @Test
    @DisplayName("Should get analysis by id")
    void shouldGetAnalysisById() {
        final UUID analysisId = UUID.randomUUID();

        final AnalysisRecordEntity entity = analysisRecordMapper.toEntity(
                "sample input",
                new AnalysisResult(
                        analysisId,
                        OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                        ErrorCategory.NULL_POINTER,
                        Severity.HIGH,
                        "A null value is being dereferenced.",
                        List.of("nullpointerexception"),
                        List.of("Check null guards"),
                        new BigDecimal("0.72"),
                        "null-pointer-rule",
                        12,
                        1
                )
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.findById(analysisId)).thenReturn(Optional.of(entity));

        final AnalysisResult result = service.getAnalysisById(analysisId);

        assertEquals(analysisId, result.analysisId());
        assertEquals(ErrorCategory.NULL_POINTER, result.category());
        assertEquals(Severity.HIGH, result.severity());
        assertEquals("null-pointer-rule", result.ruleCode());
        assertEquals(Integer.valueOf(12), result.rawInputLength());
        assertEquals(Integer.valueOf(1), result.matchedRuleCount());
        assertEquals(0, result.confidence().compareTo(new BigDecimal("0.72")));
    }

    /**
     * Verifies that the service emits the expected domain exception when the requested
     * analysis does not exist.
     */
    @Test
    @DisplayName("Should throw AnalysisNotFoundException when analysis does not exist")
    void shouldThrowAnalysisNotFoundExceptionWhenAnalysisDoesNotExist() {
        final UUID analysisId = UUID.randomUUID();

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.findById(analysisId)).thenReturn(Optional.empty());

        assertThrows(AnalysisNotFoundException.class, () -> service.getAnalysisById(analysisId));
    }

    /**
     * Verifies that the service returns all persisted analyses in the same ordering
     * provided by the repository query.
     */
    @Test
    @DisplayName("Should return all analyses ordered by repository query")
    void shouldReturnAllAnalysesOrderedByRepositoryQuery() {
        final AnalysisRecordEntity first = analysisRecordMapper.toEntity(
                "first input",
                new AnalysisResult(
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                        ErrorCategory.DATABASE_CONNECTION,
                        Severity.CRITICAL,
                        "Cannot connect to database.",
                        List.of("connection refused"),
                        List.of("Verify database availability"),
                        new BigDecimal("0.80"),
                        "database-connection-rule",
                        11,
                        1
                )
        );

        final AnalysisRecordEntity second = analysisRecordMapper.toEntity(
                "second input",
                new AnalysisResult(
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-04-12T09:00:00Z"),
                        ErrorCategory.TIMEOUT,
                        Severity.HIGH,
                        "The operation exceeded the allowed execution time.",
                        List.of("timeout"),
                        List.of("Review timeout thresholds"),
                        new BigDecimal("0.67"),
                        "timeout-rule",
                        12,
                        2
                )
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.findAllByOrderByAnalyzedAtDesc()).thenReturn(List.of(first, second));

        final List<AnalysisResult> results = service.getAllAnalyses();

        assertEquals(2, results.size());
        assertEquals("database-connection-rule", results.get(0).ruleCode());
        assertEquals("timeout-rule", results.get(1).ruleCode());
    }

    /**
     * Verifies that the paginated history use case works correctly when the standard
     * category, severity, and rule code filters are provided.
     */
    @Test
    @DisplayName("Should return paged analyses with filters")
    void shouldReturnPagedAnalysesWithFilters() {
        final AnalysisRecordEntity entity = analysisRecordMapper.toEntity(
                "database input",
                new AnalysisResult(
                        UUID.randomUUID(),
                        OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                        ErrorCategory.DATABASE_CONNECTION,
                        Severity.CRITICAL,
                        "Cannot connect to database.",
                        List.of("connection refused"),
                        List.of("Verify database availability"),
                        new BigDecimal("0.80"),
                        "database-connection-rule",
                        14,
                        1
                )
        );

        final Page<AnalysisRecordEntity> entityPage = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 10),
                1
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(entityPage);

        final Page<AnalysisResult> resultPage = service.getAnalyses(
                "DATABASE_CONNECTION",
                "CRITICAL",
                "database-connection-rule",
                null,
                null,
                0,
                10
        );

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals("database-connection-rule", resultPage.getContent().get(0).ruleCode());
        assertEquals(ErrorCategory.DATABASE_CONNECTION, resultPage.getContent().get(0).category());
        assertEquals(Severity.CRITICAL, resultPage.getContent().get(0).severity());
    }

    /**
     * Verifies that the paginated history use case also accepts a valid temporal range
     * together with the standard filters.
     */
    @Test
    @DisplayName("Should return paged analyses with filters and date range")
    void shouldReturnPagedAnalysesWithFiltersAndDateRange() {
        final AnalysisRecordEntity entity = analysisRecordMapper.toEntity(
                "unknown input",
                new AnalysisResult(
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
                )
        );

        final Page<AnalysisRecordEntity> entityPage = new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 20),
                1
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(entityPage);

        final Page<AnalysisResult> resultPage = service.getAnalyses(
                "UNKNOWN",
                "LOW",
                "unknown-fallback-rule",
                OffsetDateTime.parse("2026-04-13T00:00:00Z"),
                OffsetDateTime.parse("2026-04-19T23:59:59Z"),
                0,
                20
        );

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals("unknown-fallback-rule", resultPage.getContent().get(0).ruleCode());
        assertEquals(ErrorCategory.UNKNOWN, resultPage.getContent().get(0).category());
        assertEquals(Severity.LOW, resultPage.getContent().get(0).severity());
    }

    /**
     * Verifies that the aggregated statistics use case converts repository projections
     * into the internal statistics model.
     */
    @Test
    @DisplayName("Should return aggregated analysis statistics")
    void shouldReturnAggregatedAnalysisStatistics() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.count()).thenReturn(6L);
        when(analysisRecordRepository.countGroupedByCategory()).thenReturn(List.of(
                new TestAnalysisGroupedCountProjection("DATABASE_CONNECTION", 3L),
                new TestAnalysisGroupedCountProjection("TIMEOUT", 2L),
                new TestAnalysisGroupedCountProjection("NULL_POINTER", 1L)
        ));
        when(analysisRecordRepository.countGroupedBySeverity()).thenReturn(List.of(
                new TestAnalysisGroupedCountProjection("CRITICAL", 3L),
                new TestAnalysisGroupedCountProjection("HIGH", 2L),
                new TestAnalysisGroupedCountProjection("MEDIUM", 1L)
        ));

        final AnalysisStats stats = service.getAnalysisStats();

        assertEquals(6L, stats.totalAnalyses());
        assertEquals(3, stats.byCategory().size());
        assertEquals("DATABASE_CONNECTION", stats.byCategory().get(0).value());
        assertEquals(3L, stats.byCategory().get(0).count());
        assertEquals(3, stats.bySeverity().size());
        assertEquals("CRITICAL", stats.bySeverity().get(0).value());
        assertEquals(3L, stats.bySeverity().get(0).count());
    }

    /**
     * Verifies that invalid category filters are rejected explicitly instead of silently
     * producing empty results.
     */
    @Test
    @DisplayName("Should reject invalid category filter")
    void shouldRejectInvalidCategoryFilter() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses("NOT_A_REAL_CATEGORY", null, null, null, null, 0, 20)
        );

        assertEquals(
                "category must be one of: DATABASE_CONNECTION, AUTHENTICATION, AUTHORIZATION, PORT_IN_USE, MISSING_ENVIRONMENT_VARIABLE, NULL_POINTER, SYNTAX_ERROR, TIMEOUT, SQL_ERROR, FILE_NOT_FOUND, UNKNOWN",
                exception.getMessage()
        );
    }

    /**
     * Verifies that invalid severity filters are rejected explicitly instead of silently
     * producing empty results.
     */
    @Test
    @DisplayName("Should reject invalid severity filter")
    void shouldRejectInvalidSeverityFilter() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses(null, "NOT_A_REAL_SEVERITY", null, null, null, 0, 20)
        );

        assertEquals(
                "severity must be one of: LOW, MEDIUM, HIGH, CRITICAL",
                exception.getMessage()
        );
    }

    /**
     * Verifies that a negative page index is rejected.
     */
    @Test
    @DisplayName("Should reject negative page")
    void shouldRejectNegativePage() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses(null, null, null, null, null, -1, 20)
        );
    }

    /**
     * Verifies that non-positive page sizes are rejected.
     */
    @Test
    @DisplayName("Should reject non-positive size")
    void shouldRejectNonPositiveSize() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses(null, null, null, null, null, 0, 0)
        );
    }

    /**
     * Verifies that page sizes greater than the configured maximum are rejected.
     */
    @Test
    @DisplayName("Should reject size greater than allowed limit")
    void shouldRejectSizeGreaterThanAllowedLimit() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses(null, null, null, null, null, 0, 101)
        );
    }

    /**
     * Verifies that an invalid temporal range is rejected when the lower bound is
     * later than the upper bound.
     */
    @Test
    @DisplayName("Should reject invalid analyzedAt range")
    void shouldRejectInvalidAnalyzedAtRange() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.getAnalyses(
                        null,
                        null,
                        null,
                        OffsetDateTime.parse("2026-04-20T00:00:00Z"),
                        OffsetDateTime.parse("2026-04-19T00:00:00Z"),
                        0,
                        20
                )
        );

        assertEquals(
                "analyzedFrom must be less than or equal to analyzedTo",
                exception.getMessage()
        );
    }

    /**
     * Verifies that blank input is rejected before the analysis engine is invoked.
     */
    @Test
    @DisplayName("Should reject blank input")
    void shouldRejectBlankInput() {
        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                analysisEngine,
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        assertThrows(IllegalArgumentException.class, () -> service.analyze("   "));
    }

    /**
     * Minimal projection stub used to test grouped-count repository responses without
     * involving persistence infrastructure.
     */
    private static final class TestAnalysisGroupedCountProjection implements AnalysisGroupedCountProjection {

        private final String value;
        private final long count;

        /**
         * Creates a grouped-count projection stub.
         *
         * @param value grouped value
         * @param count grouped count
         */
        private TestAnalysisGroupedCountProjection(final String value, final long count) {
            this.value = value;
            this.count = count;
        }

        /**
         * Returns the grouped value.
         *
         * @return grouped value
         */
        @Override
        public String getValue() {
            return value;
        }

        /**
         * Returns the grouped count.
         *
         * @return grouped count
         */
        @Override
        public long getCount() {
            return count;
        }
    }
}