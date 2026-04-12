package com.rootcause.rules;

import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.util.ScoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class BasePatternRule implements AnalysisRule {

    private final String ruleCode;
    private final ErrorCategory category;
    private final Severity severity;
    private final String probableCause;
    private final List<CompiledPattern> compiledPatterns;
    private final List<String> recommendedSteps;
    private final double baseScore;

    protected BasePatternRule(String ruleCode,
                              ErrorCategory category,
                              Severity severity,
                              String probableCause,
                              Map<String, String> patternDefinitions,
                              List<String> recommendedSteps,
                              double baseScore) {
        this.ruleCode = ruleCode;
        this.category = category;
        this.severity = severity;
        this.probableCause = probableCause;
        this.recommendedSteps = List.copyOf(recommendedSteps);
        this.baseScore = baseScore;
        this.compiledPatterns = compile(patternDefinitions);
    }

    @Override
    public RuleMatch evaluate(AnalysisRequestContext context) {
        List<String> matchedPatterns = new ArrayList<>();

        for (CompiledPattern compiledPattern : compiledPatterns) {
            if (compiledPattern.pattern().matcher(context.originalText()).find()) {
                matchedPatterns.add(compiledPattern.label());
            }
        }

        if (matchedPatterns.isEmpty()) {
            return new RuleMatch(
                    ruleCode,
                    category,
                    severity,
                    0.0,
                    probableCause,
                    List.of(),
                    List.of()
            );
        }

        double score = ScoreUtils.clamp(
                baseScore + Math.max(0, matchedPatterns.size() - 1) * 0.08,
                0.0,
                0.99
        );

        return new RuleMatch(
                ruleCode,
                category,
                severity,
                score,
                probableCause,
                matchedPatterns,
                recommendedSteps
        );
    }

    private List<CompiledPattern> compile(Map<String, String> patternDefinitions) {
        List<CompiledPattern> compiled = new ArrayList<>();

        for (Map.Entry<String, String> entry : patternDefinitions.entrySet()) {
            compiled.add(new CompiledPattern(
                    entry.getKey(),
                    Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
            ));
        }

        return List.copyOf(compiled);
    }

    private record CompiledPattern(String label, Pattern pattern) {
    }
}