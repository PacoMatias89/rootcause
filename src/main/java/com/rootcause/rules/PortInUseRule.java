package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects port-in-use-related failures.
 *
 * <p>This rule identifies situations where the application attempts to bind
 * to a network port that is already occupied by another process or by another
 * instance of the same service.</p>
 *
 * <p>Typical causes include duplicated local execution, conflicting services,
 * container port collisions, or orchestrator/network configuration issues.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 40}.</p>
 */
@Component
@Order(40)
public class PortInUseRule extends BasePatternRule {

    /**
     * Creates the port-in-use rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public PortInUseRule() {
        super(
                "port-in-use-rule",
                ErrorCategory.PORT_IN_USE,
                Severity.HIGH,
                "The application is trying to bind to a port that is already occupied by another process or another instance of the same service.",
                patterns(),
                List.of(
                        "Identify the process currently using the port.",
                        "Stop the conflicting process or change the application port.",
                        "Check whether another instance of the same service is already running.",
                        "Review local startup scripts, container mappings, and orchestrator port assignments."
                ),
                0.74
        );
    }

    /**
     * Defines the port-in-use-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("address already in use", "address\\s+already\\s+in\\s+use");
        patterns.put("bind exception", "bindexception");
        patterns.put("port already used", "port\\s+\\d+.*already.*use");
        patterns.put("failed to bind", "failed\\s+to\\s+bind");
        return patterns;
    }
}