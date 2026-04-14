package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects database connection failures.
 *
 * <p>This rule identifies situations where the application cannot establish
 * a connection to the target database.</p>
 *
 * <p>Typical causes include an unreachable database server, incorrect connection
 * parameters, invalid credentials, network connectivity problems, or issues in
 * the JDBC connection configuration.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 10}.</p>
 */
@Component
@Order(10)
public class DatabaseConnectionRule extends BasePatternRule {

    /**
     * Creates the database connection rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public DatabaseConnectionRule() {
        super(
                "database-connection-rule",
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "The application cannot establish a connection to the database. The most likely causes are an unreachable database server, wrong connection parameters, invalid credentials, or a network/connectivity issue.",
                patterns(),
                List.of(
                        "Verify database host, port, database name, username, and password.",
                        "Confirm the database instance is running and reachable from the application host.",
                        "Check network routes, firewall rules, VPN policies, or container-to-database connectivity.",
                        "Review the JDBC URL, connection pool settings, and any SSL requirements."
                ),
                0.72
        );
    }

    /**
     * Defines the database-connection-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("connection refused", "connection\\s+refused");
        patterns.put("failed to obtain jdbc connection", "failed\\s+to\\s+obtain\\s+jdbc\\s+connection");
        patterns.put("could not open connection", "could\\s+not\\s+open\\s+connection");
        patterns.put("communications link failure", "communications\\s+link\\s+failure");
        patterns.put("jdbc connect failure", "jdbc:.*|driver.*connect|unable\\s+to\\s+connect");
        return patterns;
    }
}