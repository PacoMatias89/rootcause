package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects missing-environment-variable and unresolved-configuration failures.
 *
 * <p>This rule identifies situations where the application depends on an environment
 * variable or configuration placeholder that is missing, empty, or not resolved correctly
 * during startup or runtime.</p>
 *
 * <p>Typical causes include missing environment variables, wrong variable names,
 * incorrect casing, broken interpolation syntax, or deployment/runtime configurations
 * that do not expose the required value to the process.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 50}.</p>
 */
@Component
@Order(50)
public class MissingEnvironmentVariableRule extends BasePatternRule {

    /**
     * Creates the missing-environment-variable rule with its fixed metadata,
     * supported patterns, recommended remediation steps, and base score.
     */
    public MissingEnvironmentVariableRule() {
        super(
                "missing-environment-variable-rule",
                ErrorCategory.MISSING_ENVIRONMENT_VARIABLE,
                Severity.HIGH,
                "A required environment variable or configuration placeholder is missing, empty, or not being resolved correctly during startup or execution.",
                patterns(),
                List.of(
                        "Verify that the required environment variable exists in the target runtime.",
                        "Check naming, casing, and interpolation syntax in configuration files.",
                        "Review deployment manifests, CI/CD variables, container env sections, and secrets/config maps.",
                        "Confirm the variable is available to the process at startup time."
                ),
                0.76
        );
    }

    /**
     * Defines the missing-environment-variable-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("environment variable not set", "environment\\s+variable.*not\\s+set");
        patterns.put("missing required environment variable", "missing\\s+required\\s+environment\\s+variable");
        patterns.put("could not resolve placeholder", "could\\s+not\\s+resolve\\s+placeholder");
        patterns.put("missing configuration value", "no\\s+value\\s+provided|missing\\s+configuration");
        return patterns;
    }
}