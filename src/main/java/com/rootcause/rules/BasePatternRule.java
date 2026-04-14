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

/**
 * Base implementation for pattern-based analysis rules.
 *
 * <p>This abstract class provides the common behavior for rules that classify
 * technical failures by matching regular-expression patterns against the analyzed input.</p>
 *
 * <p>Each concrete rule supplies its own:</p>
 * <ul>
 *     <li>rule code</li>
 *     <li>error category</li>
 *     <li>severity</li>
 *     <li>probable cause</li>
 *     <li>pattern definitions</li>
 *     <li>recommended remediation steps</li>
 *     <li>base score</li>
 * </ul>
 *
 * <p>The evaluation flow implemented here is:</p>
 * <ol>
 *     <li>compile the configured regex patterns once during construction</li>
 *     <li>evaluate the incoming analysis context against all compiled patterns</li>
 *     <li>collect all matched pattern labels</li>
 *     <li>return a zero-score {@link RuleMatch} when nothing matches</li>
 *     <li>calculate a final score when one or more patterns match</li>
 *     <li>return a populated {@link RuleMatch} with category, severity, cause, patterns, and recommendations</li>
 * </ol>
 *
 * <p>The final score starts from the configured base score and increases slightly
 * when additional patterns are matched, while remaining clamped to the accepted range.</p>
 */
public abstract class BasePatternRule implements AnalysisRule {

    private final String ruleCode;
    private final ErrorCategory category;
    private final Severity severity;
    private final String probableCause;
    private final List<CompiledPattern> compiledPatterns;
    private final List<String> recommendedSteps;
    private final double baseScore;

    /**
     * Creates a pattern-based rule with the fixed metadata and pattern configuration
     * required for evaluation.
     *
     * @param ruleCode stable internal identifier of the rule
     * @param category category assigned by this rule
     * @param severity severity assigned by this rule
     * @param probableCause human-readable explanation of the most likely cause
     * @param patternDefinitions ordered map of pattern labels and regex expressions
     * @param recommendedSteps remediation steps suggested when this rule matches
     * @param baseScore initial score assigned when the rule matches at least one pattern
     */
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

    /**
     * Evaluates the provided analysis context against all compiled patterns of the rule.
     *
     * <p>When no pattern matches, the returned {@link RuleMatch} has score {@code 0.0}
     * and empty diagnostic lists so the caller can ignore it safely.</p>
     *
     * <p>When one or more patterns match, the score is computed from the configured
     * base score plus a small increment for each additional matched pattern, and then
     * clamped to the allowed range.</p>
     *
     * @param context prepared analysis request context containing the input to inspect
     * @return rule evaluation result for this pattern-based rule
     */
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

    /**
     * Compiles the configured pattern definitions into immutable compiled pattern objects.
     *
     * <p>All regex patterns are compiled with case-insensitive, multiline, and dotall flags
     * so they can match technical input more flexibly across line breaks and text variations.</p>
     *
     * @param patternDefinitions ordered map of pattern labels and regex expressions
     * @return immutable list of compiled patterns ready for evaluation
     */
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

    /**
     * Internal immutable pair containing the human-readable pattern label and its compiled regex.
     *
     * @param label label exposed when the pattern is detected
     * @param pattern compiled regex used for matching
     */
    private record CompiledPattern(String label, Pattern pattern) {
    }
}