package com.rootcause.service;

import com.rootcause.model.AnalysisDecision;
import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.rules.AnalysisRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link RuleBasedAnalysisEngine}.
 *
 * <p>These tests verify the behavior of the rule-based diagnostic engine, including
 * best-match selection, fallback generation, tie-breaking by severity, and constructor validation.</p>
 */
class RuleBasedAnalysisEngineTest {

    @Test
    @DisplayName("Should select strongest rule and keep matched rule count")
    void shouldSelectStrongestRuleAndKeepMatchedRuleCount() {
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

        final RuleBasedAnalysisEngine engine = new RuleBasedAnalysisEngine(
                List.of(weakerRule, strongerRule)
        );

        final AnalysisDecision decision = engine.analyze(
                new AnalysisRequestContext(
                        "org.postgresql.util.PSQLException: Connection refused",
                        "org.postgresql.util.psqlexception: connection refused"
                )
        );

        assertEquals("database-connection-rule", decision.bestMatch().ruleCode());
        assertEquals(ErrorCategory.DATABASE_CONNECTION, decision.bestMatch().category());
        assertEquals(Severity.CRITICAL, decision.bestMatch().severity());
        assertEquals(2, decision.matchedRuleCount());
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

        final RuleBasedAnalysisEngine engine = new RuleBasedAnalysisEngine(
                List.of(noMatchRule)
        );

        final AnalysisDecision decision = engine.analyze(
                new AnalysisRequestContext("something unrecognized", "something unrecognized")
        );

        assertEquals("unknown-fallback-rule", decision.bestMatch().ruleCode());
        assertEquals(ErrorCategory.UNKNOWN, decision.bestMatch().category());
        assertEquals(Severity.LOW, decision.bestMatch().severity());
        assertEquals(0, decision.matchedRuleCount());
    }

    @Test
    @DisplayName("Should break score ties using severity priority")
    void shouldBreakScoreTiesUsingSeverityPriority() {
        final AnalysisRule mediumSeverityRule = context -> new RuleMatch(
                "timeout-rule",
                ErrorCategory.TIMEOUT,
                Severity.MEDIUM,
                0.70,
                "Timeout detected.",
                List.of("timeout"),
                List.of("Review timeout thresholds")
        );

        final AnalysisRule criticalSeverityRule = context -> new RuleMatch(
                "database-connection-rule",
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                0.70,
                "The application cannot establish a connection to the database.",
                List.of("connection refused"),
                List.of("Verify database availability")
        );

        final RuleBasedAnalysisEngine engine = new RuleBasedAnalysisEngine(
                List.of(mediumSeverityRule, criticalSeverityRule)
        );

        final AnalysisDecision decision = engine.analyze(
                new AnalysisRequestContext("sample input", "sample input")
        );

        assertEquals("database-connection-rule", decision.bestMatch().ruleCode());
        assertEquals(Severity.CRITICAL, decision.bestMatch().severity());
        assertEquals(2, decision.matchedRuleCount());
    }

    @Test
    @DisplayName("Should reject empty rule list in constructor")
    void shouldRejectEmptyRuleListInConstructor() {
        assertThrows(IllegalArgumentException.class, () ->
                new RuleBasedAnalysisEngine(List.of())
        );
    }
}