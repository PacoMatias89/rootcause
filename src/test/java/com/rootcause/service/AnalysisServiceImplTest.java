package com.rootcause.service;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.AnalysisDecision;
import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.repository.AnalysisRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
 * and filter validation.</p>
 */
class AnalysisServiceImplTest {

    private AnalysisEngine analysisEngine;
    private AnalysisRecordRepository analysisRecordRepository;
    private AnalysisRecordMapper analysisRecordMapper;
    private Clock clock;

    @BeforeEach
    void setUp() {
        analysisEngine = mock(AnalysisEngine.class);
        analysisRecordRepository = mock(AnalysisRecordRepository.class);
        analysisRecordMapper = new AnalysisRecordMapper();
        clock = Clock.fixed(Instant.parse("2026-04-13T10:15:30Z"), ZoneOffset.UTC);
    }

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

        when(analysisRecordRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(entityPage);

        final Page<AnalysisResult> resultPage = service.getAnalyses(
                "DATABASE_CONNECTION",
                "CRITICAL",
                "database-connection-rule",
                0,
                10
        );

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals("database-connection-rule", resultPage.getContent().get(0).ruleCode());
        assertEquals(ErrorCategory.DATABASE_CONNECTION, resultPage.getContent().get(0).category());
        assertEquals(Severity.CRITICAL, resultPage.getContent().get(0).severity());
    }

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
                service.getAnalyses("NOT_A_REAL_CATEGORY", null, null, 0, 20)
        );

        assertEquals(
                "category must be one of: DATABASE_CONNECTION, AUTHENTICATION, AUTHORIZATION, PORT_IN_USE, MISSING_ENVIRONMENT_VARIABLE, NULL_POINTER, SYNTAX_ERROR, TIMEOUT, SQL_ERROR, FILE_NOT_FOUND, UNKNOWN",
                exception.getMessage()
        );
    }

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
                service.getAnalyses(null, "NOT_A_REAL_SEVERITY", null, 0, 20)
        );

        assertEquals(
                "severity must be one of: LOW, MEDIUM, HIGH, CRITICAL",
                exception.getMessage()
        );
    }

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
                service.getAnalyses(null, null, null, -1, 20)
        );
    }

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
                service.getAnalyses(null, null, null, 0, 0)
        );
    }

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
                service.getAnalyses(null, null, null, 0, 101)
        );
    }

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
}