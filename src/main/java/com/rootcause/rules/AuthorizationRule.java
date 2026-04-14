package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects authorization-related failures.
 *
 * <p>This rule identifies situations where the caller is authenticated, or at least
 * partially identified, but does not have sufficient permissions to perform the
 * requested operation.</p>
 *
 * <p>Typical cases include forbidden access, missing privileges, denied operations,
 * and permission model mismatches between the caller and the protected resource.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 30}.</p>
 */
@Component
@Order(30)
public class AuthorizationRule extends BasePatternRule {

    /**
     * Creates the authorization rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public AuthorizationRule() {
        super(
                "authorization-rule",
                ErrorCategory.AUTHORIZATION,
                Severity.HIGH,
                "The caller is authenticated or partially identified, but does not have enough permissions to execute the requested action.",
                patterns(),
                List.of(
                        "Verify roles, permissions, scopes, and access policies assigned to the caller.",
                        "Check whether the resource requires a stronger privilege than the current account has.",
                        "Review application authorization rules, gateway policies, and RBAC/ABAC settings.",
                        "Inspect the denied operation and compare it with the intended permission model."
                ),
                0.68
        );
    }

    /**
     * Defines the authorization-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("access denied", "access\\s+denied");
        patterns.put("forbidden", "\\b403\\b|forbidden");
        patterns.put("permission denied", "permission\\s+denied");
        patterns.put("not authorized", "not\\s+authorized|unauthorized\\s+operation");
        patterns.put("insufficient privileges", "insufficient\\s+privileges");
        return patterns;
    }
}