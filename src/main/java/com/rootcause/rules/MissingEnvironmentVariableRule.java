package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(50)
public class MissingEnvironmentVariableRule extends BasePatternRule {

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

    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("environment variable not set", "environment\\s+variable.*not\\s+set");
        patterns.put("missing required environment variable", "missing\\s+required\\s+environment\\s+variable");
        patterns.put("could not resolve placeholder", "could\\s+not\\s+resolve\\s+placeholder");
        patterns.put("missing configuration value", "no\\s+value\\s+provided|missing\\s+configuration");
        return patterns;
    }
}