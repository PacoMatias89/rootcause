package com.rootcause.model;
import java.util.Objects;

/**
 * Internal decision produced by the analysis engine before persistence.
 *
 * <p>This type encapsulates the selected best match for a request together with
 * the number of positively matched rules evaluated during the diagnostic phase.</p>
 *
 * <p>It is an internal model used to decouple diagnosis generation from the
 * application service orchestration.</p>
 *
 * @param bestMatch selected best rule match
 * @param matchedRuleCount number of positively matched rules
 */
public record AnalysisDecision(
        RuleMatch bestMatch,
        int matchedRuleCount
) {

    /**
     * Creates an immutable analysis decision.
     *
     * @param bestMatch selected best rule match
     * @param matchedRuleCount number of positively matched rules
     * @throws NullPointerException when {@code bestMatch} is {@code null}
     * @throws IllegalArgumentException when {@code matchedRuleCount} is negative
     */

    public AnalysisDecision{
        bestMatch = Objects.requireNonNull(bestMatch, "bestMatch must not be null");

        if (matchedRuleCount < 0) {
            throw new IllegalArgumentException("matchedRuleCount must be greater than or equal to 0");
        }
    }
}
