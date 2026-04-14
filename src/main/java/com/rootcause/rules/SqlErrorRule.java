package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects SQL-related failures.
 *
 * <p>This rule identifies situations where an SQL statement or a database interaction
 * fails during execution.</p>
 *
 * <p>Typical causes include malformed SQL, schema mismatches, missing tables or columns,
 * invalid SQL grammar, incorrect parameter binding, or vendor-specific SQL errors.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 90}.</p>
 */
@Component
@Order(90)
public class SqlErrorRule extends BasePatternRule {

    /**
     * Creates the SQL error rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public SqlErrorRule() {
        super(
                "sql-error-rule",
                ErrorCategory.SQL_ERROR,
                Severity.HIGH,
                "The SQL statement or database interaction failed. The probable causes are malformed SQL, schema mismatch, missing columns/tables, invalid SQL grammar, or vendor-specific SQL issues.",
                patterns(),
                List.of(
                        "Inspect the SQL statement and the database error message together.",
                        "Verify table names, column names, aliases, and parameter bindings.",
                        "Check whether the schema version matches the application version.",
                        "Review migrations, naming conventions, and vendor-specific SQL syntax."
                ),
                0.71
        );
    }

    /**
     * Defines the SQL-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("SQLSyntaxErrorException", "sqlsyntaxerrorexception");
        patterns.put("SQLState", "sqlstate");
        patterns.put("PSQLException", "psqlexception");
        patterns.put("ORA vendor error", "ora-\\d+");
        patterns.put("SQL grammar error", "you\\s+have\\s+an\\s+error\\s+in\\s+your\\s+sql\\s+syntax");
        patterns.put("column does not exist", "column.*does\\s+not\\s+exist");
        patterns.put("relation does not exist", "relation.*does\\s+not\\s+exist");
        return patterns;
    }
}