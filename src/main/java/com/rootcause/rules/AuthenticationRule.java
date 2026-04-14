package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects authentication-related failures.
 *
 * <p>This rule identifies technical errors that indicate the system could not
 * verify the identity of the caller successfully. Typical causes include invalid
 * credentials, expired credentials, incorrect authentication schemes, or broken
 * login configuration.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 20}.</p>
 */
@Component
@Order(20)
public class AuthenticationRule extends BasePatternRule {

    /**
     * Creates the authentication rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public AuthenticationRule() {
        super(
                "authentication-rule",
                ErrorCategory.AUTHENTICATION,
                Severity.HIGH,
                "The system failed to verify the identity of the caller. The probable causes are invalid credentials, expired credentials, a wrong authentication mechanism, or a broken login configuration.",
                patterns(),
                List.of(
                        "Verify username, password, token, API key, or client secret.",
                        "Check whether the credential has expired or has been rotated.",
                        "Confirm the expected authentication scheme, such as Basic, Bearer, OAuth2, or session-based login.",
                        "Review identity provider logs and application-side authentication configuration."
                ),
                0.70
        );
    }

    /**
     * Defines the authentication-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("authentication failed", "authentication\\s+failed");
        patterns.put("invalid credentials", "invalid\\s+credentials");
        patterns.put("bad credentials", "bad\\s+credentials");
        patterns.put("login failed", "login\\s+failed");
        patterns.put("password authentication failed", "password\\s+authentication\\s+failed");
        return patterns;
    }
}