package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(70)
public class SyntaxErrorRule extends BasePatternRule {

    public SyntaxErrorRule() {
        super(
                "syntax-error-rule",
                ErrorCategory.SYNTAX_ERROR,
                Severity.MEDIUM,
                "The input contains invalid syntax for the target language, parser, or compiler. The most likely causes are malformed statements, unsupported tokens, or broken structure.",
                patterns(),
                List.of(
                        "Inspect the exact line and column reported by the parser or compiler.",
                        "Check for missing separators, brackets, quotes, or delimiters.",
                        "Validate the syntax against the exact language or version in use.",
                        "Reduce the failing input to the smallest reproducible example."
                ),
                0.58
        );
    }

    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("syntax error", "syntax\\s+error");
        patterns.put("unexpected token", "unexpected\\s+token");
        patterns.put("parse error", "parse\\s+error");
        patterns.put("illegal start of expression", "illegal\\s+start\\s+of\\s+expression");
        patterns.put("compilation failed", "compilation\\s+failed");
        return patterns;
    }
}