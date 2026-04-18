package com.rootcause.service;

import com.rootcause.model.AnalysisDecision;
import com.rootcause.model.AnalysisRequestContext;

/**
 * Internal contract responsible for generating a technical diagnosis from a prepared request context.
 *
 * <p>This abstraction allows the application to support different diagnosis strategies over time,
 * such as rule-based analysis, AI-assisted analysis, or hybrid approaches, without changing the
 * public API or the orchestration responsibilities of {@link AnalysisServiceImpl}.</p>
 */
public interface AnalysisEngine {
    /**
     * Generates an internal diagnosis decision for the provided request context.
     *
     * @param context prepared analysis request context
     * @return internal decision containing the selected match and matched rule count
     */
    AnalysisDecision analyze(AnalysisRequestContext context);
}
