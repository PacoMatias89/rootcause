package com.rootcause.rules;

import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.RuleMatch;

/**
 * Contract for a single analysis rule in the RootCause rule engine.
 *
 * <p>An implementation of this interface evaluates a prepared
 * {@link AnalysisRequestContext} and returns a {@link RuleMatch}
 * describing the outcome of that evaluation.</p>
 *
 * <p>Each rule is responsible for detecting a specific technical failure
 * pattern or diagnostic scenario and assigning its own category, severity,
 * probable cause, detected patterns, recommended steps, and score.</p>
 */
public interface AnalysisRule {

    /**
     * Evaluates the provided analysis context and returns the rule match result.
     *
     * @param context prepared analysis request context containing the raw and normalized input
     * @return rule evaluation result for this specific rule
     */
    RuleMatch evaluate(AnalysisRequestContext context);
}