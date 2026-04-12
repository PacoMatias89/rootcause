package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(20)
public class AuthenticationRule extends BasePatternRule {

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