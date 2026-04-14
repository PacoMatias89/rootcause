package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects timeout-related failures.
 *
 * <p>This rule identifies situations where an operation exceeds the maximum
 * allowed execution time and is interrupted or reported as timed out.</p>
 *
 * <p>Typical causes include slow downstream dependencies, network latency,
 * overloaded resources, blocking operations, insufficient thread availability,
 * or timeout thresholds that are too strict for the current workload.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 80}.</p>
 */
@Component
@Order(80)
public class TimeoutRule extends BasePatternRule {

    /**
     * Creates the timeout rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public TimeoutRule() {
        super(
                "timeout-rule",
                ErrorCategory.TIMEOUT,
                Severity.HIGH,
                "An operation exceeded the allowed time limit. The probable causes are slow downstream dependencies, network latency, blocking operations, or an incorrectly tuned timeout configuration.",
                patterns(),
                List.of(
                        "Identify which operation timed out: database, HTTP call, external API, file system, or internal processing.",
                        "Measure latency and check whether the timeout threshold is too strict for the current workload.",
                        "Inspect downstream service health, retries, connection pools, and thread usage.",
                        "Review resource saturation, deadlocks, and blocking code paths."
                ),
                0.69
        );
    }

    /**
     * Defines the timeout-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("timed out", "timed\\s+out");
        patterns.put("timeout exception", "timeout\\s+exception|timeoutexception");
        patterns.put("read timed out", "read\\s+timed\\s+out");
        patterns.put("connect timed out", "connect\\s+timed\\s+out");
        patterns.put("deadline exceeded", "deadline\\s+exceeded");
        return patterns;
    }
}