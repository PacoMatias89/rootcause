package com.rootcause.service;

import com.rootcause.model.AnalysisDecision;
import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.rules.AnalysisRule;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Rule-based implementation of {@link AnalysisEngine}.
 *
 * <p>This engine evaluates all configured {@link AnalysisRule} instances, keeps only the
 * positively matched ones, selects the strongest match using score and severity priority,
 * and produces an internal {@link AnalysisDecision} for the application service.</p>
 *
 * <p>When no rule matches strongly enough, the engine falls back to a predefined
 * low-confidence {@code UNKNOWN} diagnosis.</p>
 */
@Service
public class RuleBasedAnalysisEngine implements AnalysisEngine {

    private final List<AnalysisRule> rules;

    /**
     * Creates the engine with the configured rule set.
     *
     * @param rules configured analysis rules available in the application context
     * @throws IllegalArgumentException when the rule list is {@code null} or empty
     */
    public RuleBasedAnalysisEngine(final List<AnalysisRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("At least one analysis rule must be configured");
        }

        this.rules = List.copyOf(rules);
    }

    /**
     * Generates a rule-based diagnosis decision from the provided request context.
     *
     * @param context prepared analysis request context
     * @return internal decision containing the selected best match and matched rule count
     */
    @Override
    public AnalysisDecision analyze(final AnalysisRequestContext context) {
        final List<RuleMatch> matchedRules = findMatchedRules(context);
        final RuleMatch bestMatch = matchedRules.isEmpty()
                ? buildFallbackMatch()
                : selectBestMatch(matchedRules);

        return new AnalysisDecision(bestMatch, matchedRules.size());
    }

    /**
     * Builds the fallback rule match used when no known rule matches the input strongly enough.
     *
     * @return fallback rule match with {@link ErrorCategory#UNKNOWN}, {@link Severity#LOW},
     * and generic diagnostic recommendations
     */
    private RuleMatch buildFallbackMatch() {
        return new RuleMatch(
                "unknown-fallback-rule",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.15,
                "The input does not match any known rule strongly enough. This may be a new failure pattern or the provided text may be too short or too incomplete.",
                List.of("no strong rule match"),
                List.of(
                        "Provide the full stack trace or a larger log excerpt.",
                        "Include the first exception line and the most relevant caused-by section.",
                        "Add technical context such as service name, environment, recent change, and timestamp."
                )
        );
    }

    /**
     * Selects the strongest rule from the provided matches.
     *
     * <p>The best match is determined first by highest score and, in case of a tie,
     * by highest severity priority.</p>
     *
     * @param matches non-empty list of positive rule matches
     * @return strongest available rule match
     * @throws IllegalStateException when the list is empty
     */
    private RuleMatch selectBestMatch(final List<RuleMatch> matches) {
        return matches.stream()
                .max(Comparator
                        .comparingDouble(RuleMatch::score)
                        .thenComparing(match -> severityPriority(match.severity())))
                .orElseThrow(() -> new IllegalStateException("No rule match available to select"));
    }

    /**
     * Evaluates all configured rules and keeps only the matches with a positive score.
     *
     * <p>This method also protects against unexpected {@code null} matches returned by any rule.</p>
     *
     * @param context prepared analysis request context
     * @return immutable list of positively matched rules
     */
    private List<RuleMatch> findMatchedRules(final AnalysisRequestContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(match -> match != null && match.score() > 0.0)
                .toList();
    }

    /**
     * Returns the numeric priority used to break ties between rule matches with the same score.
     *
     * <p>A higher severity implies a higher priority.</p>
     *
     * @param severity severity to rank
     * @return numeric priority for tie-breaking purposes
     */
    private int severityPriority(final Severity severity) {
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}