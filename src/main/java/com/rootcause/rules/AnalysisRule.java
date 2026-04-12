package com.rootcause.rules;

import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.RuleMatch;

public interface AnalysisRule {

    RuleMatch evaluate(AnalysisRequestContext context);
}