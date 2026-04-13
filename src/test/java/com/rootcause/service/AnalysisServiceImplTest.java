package com.rootcause.service;

import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.repository.AnalysisRecordRepository;
import com.rootcause.rules.AnalysisRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

class AnalysisServiceImplTest {

    private AnalysisRecordRepository analysisRecordRepository;
    private AnalysisRecordMapper analysisRecordMapper;
    private Clock clock;

    @BeforeEach
    void setUp() {
        analysisRecordRepository = mock(AnalysisRecordRepository.class);
        analysisRecordMapper = new AnalysisRecordMapper();
        clock = Clock.fixed(Instant.parse("2026-04-13T10:15:30Z"), ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Should analyze input and persist metadata from best rule")
    void shouldAnalyzeInputAndPersistMetadataFromBestRule() {
        final AnalysisRule weakerRule = context -> new RuleMatch(
                "sql-error-rule",
                ErrorCategory.SQL_ERROR,
                Severity.HIGH,
                0.55,
                "Generic SQL issue detected.",
                List.of("sql exception"),
                List.of("Review SQL statement")
        );

        final AnalysisRule strongerRule = context -> new RuleMatch(
                "database-connection-rule",
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                0.80,
                "The application cannot establish a connection to the database.",
                List.of("connection refused", "failed to obtain jdbc connection"),
                List.of("Verify database availability", "Check datasource configuration")
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(weakerRule, strongerRule),
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

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

        verify(analysisRecordRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should use fallback when no rule matches")
    void shouldUseFallbackWhenNoRuleMatches() {
        final AnalysisRule noMatchRule = context -> new RuleMatch(
                "some-rule",
                ErrorCategory.TIMEOUT,
                Severity.MEDIUM,
                0.0,
                "No effective match",
                List.of(),
                List.of()
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(noMatchRule),
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        when(analysisRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final String inputText = "something unrecognized";
        final AnalysisResult result = service.analyze(inputText);

        assertEquals(ErrorCategory.UNKNOWN, result.category());
        assertEquals(Severity.LOW, result.severity());
        assertEquals("unknown-fallback-rule", result.ruleCode());
        assertEquals(Integer.valueOf(inputText.trim().length()), result.rawInputLength());
        assertEquals(Integer.valueOf(0), result.matchedRuleCount());
        assertEquals(0, result.confidence().compareTo(new BigDecimal("0.15")));

        verify(analysisRecordRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should get analysis by id")
    void shouldGetAnalysisById() {
        final UUID analysisId = UUID.randomUUID();

        final var entity = analysisRecordMapper.toEntity(
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

        final AnalysisRule dummyRule = context -> new RuleMatch(
                "dummy",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.1,
                "dummy",
                List.of(),
                List.of()
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(dummyRule),
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

        final AnalysisRule dummyRule = context -> new RuleMatch(
                "dummy",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.1,
                "dummy",
                List.of(),
                List.of()
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(dummyRule),
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
        final var first = analysisRecordMapper.toEntity(
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

        final var second = analysisRecordMapper.toEntity(
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

        final AnalysisRule dummyRule = context -> new RuleMatch(
                "dummy",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.1,
                "dummy",
                List.of(),
                List.of()
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(dummyRule),
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
    @DisplayName("Should reject blank input")
    void shouldRejectBlankInput() {
        final AnalysisRule dummyRule = context -> new RuleMatch(
                "dummy",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.1,
                "dummy",
                List.of(),
                List.of()
        );

        final AnalysisServiceImpl service = new AnalysisServiceImpl(
                List.of(dummyRule),
                analysisRecordRepository,
                analysisRecordMapper,
                clock
        );

        assertThrows(IllegalArgumentException.class, () -> service.analyze("   "));
    }

    @Test
    @DisplayName("Should reject empty rule list in constructor")
    void shouldRejectEmptyRuleListInConstructor() {
        assertThrows(IllegalArgumentException.class, () ->
                new AnalysisServiceImpl(
                        List.of(),
                        analysisRecordRepository,
                        analysisRecordMapper,
                        clock
                )
        );
    }
}