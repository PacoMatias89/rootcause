package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects null-pointer-related failures.
 *
 * <p>This rule identifies situations where the application attempts to dereference
 * a {@code null} reference at runtime.</p>
 *
 * <p>Typical causes include missing object initialization, failed dependency injection,
 * unexpected null inputs, invalid object lifecycle handling, or absent defensive validation
 * before accessing an object.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 60}.</p>
 */
@Component
@Order(60)
public class NullPointerRule extends BasePatternRule {

    /**
     * Creates the null-pointer rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public NullPointerRule() {
        super(
                "null-pointer-rule",
                ErrorCategory.NULL_POINTER,
                Severity.HIGH,
                "The code is dereferencing a null reference. The probable causes are missing initialization, absent dependency injection, unexpected null input, or an invalid object lifecycle.",
                patterns(),
                List.of(
                        "Inspect the first stack trace line in your own code, not only the framework lines.",
                        "Verify object initialization, dependency injection, and constructor wiring.",
                        "Add null checks or defensive validation at the boundary where the value enters the flow.",
                        "Trace where the null value is produced and why it was not handled earlier."
                ),
                0.78
        );
    }

    /**
     * Defines the null-pointer-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("NullPointerException", "nullpointerexception");
        patterns.put("cannot invoke because is null", "cannot\\s+invoke.*because.*is\\s+null");
        patterns.put("null object reference", "null\\s+object\\s+reference");
        return patterns;
    }
}