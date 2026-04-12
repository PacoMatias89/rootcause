package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(30)
public class AuthorizationRule extends BasePatternRule {

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