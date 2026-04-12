package com.rootcause.service;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import com.rootcause.repository.AnalysisRecordRepository;
import com.rootcause.rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisServiceImplTest {

    private AnalysisRecordRepository repository;
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        repository = mock(AnalysisRecordRepository.class);
        when(repository.save(any(AnalysisRecordEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-12T10:15:30Z"), ZoneOffset.UTC);

        analysisService = new AnalysisServiceImpl(
                defaultRules(),
                repository,
                new AnalysisRecordMapper(),
                fixedClock
        );
    }

    @Test
    void shouldClassifyDatabaseConnectionError() {
        String input = """
                org.postgresql.util.PSQLException: Connection refused
                Failed to obtain JDBC Connection
                """;

        AnalysisResult result = analysisService.analyze(input);

        assertEquals(ErrorCategory.DATABASE_CONNECTION, result.category());
        assertEquals(Severity.CRITICAL, result.severity());
        assertEquals(new BigDecimal("0.80"), result.confidence());
        assertTrue(result.detectedPatterns().contains("connection refused"));
        assertTrue(result.detectedPatterns().contains("failed to obtain jdbc connection"));

        verify(repository, times(1)).save(any(AnalysisRecordEntity.class));
    }

    @Test
    void shouldReturnUnknownWhenNoRuleMatches() {
        String input = "This is a vague technical note without enough diagnostic detail.";

        AnalysisResult result = analysisService.analyze(input);

        assertEquals(ErrorCategory.UNKNOWN, result.category());
        assertEquals(Severity.LOW, result.severity());
        assertEquals(new BigDecimal("0.15"), result.confidence());
        assertEquals(List.of("no strong rule match"), result.detectedPatterns());

        verify(repository, times(1)).save(any(AnalysisRecordEntity.class));
    }

    private List<AnalysisRule> defaultRules() {
        return List.of(
                new DatabaseConnectionRule(),
                new AuthenticationRule(),
                new AuthorizationRule(),
                new PortInUseRule(),
                new MissingEnvironmentVariableRule(),
                new NullPointerRule(),
                new SyntaxErrorRule(),
                new TimeoutRule(),
                new SqlErrorRule(),
                new FileNotFoundRule()
        );
    }
}