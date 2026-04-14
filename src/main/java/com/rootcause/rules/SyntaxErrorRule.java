package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects syntax-related failures.
 *
 * <p>This rule identifies situations where the provided input contains invalid
 * syntax for the target language, parser, or compiler.</p>
 *
 * <p>Typical causes include malformed statements, unsupported tokens, missing
 * delimiters, broken structural elements, or invalid constructs for the specific
 * language version being processed.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 70}.</p>
 */
@Component
@Order(70)
public class SyntaxErrorRule extends BasePatternRule {

    /**
     * Creates the syntax error rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
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

    /**
     * Defines the syntax-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
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