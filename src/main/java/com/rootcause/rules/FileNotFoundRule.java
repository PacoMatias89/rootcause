package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rule that detects file-not-found-related failures.
 *
 * <p>This rule identifies situations where the application attempts to access
 * a file or path that is not available at runtime.</p>
 *
 * <p>Typical causes include missing files, incorrect paths, misspelled filenames,
 * wrong working-directory assumptions, or missing mounted volumes in containerized
 * or deployed environments.</p>
 *
 * <p>The rule is registered as a Spring component and participates in the
 * ordered rule evaluation pipeline with priority {@code 100}.</p>
 */
@Component
@Order(100)
public class FileNotFoundRule extends BasePatternRule {

    /**
     * Creates the file-not-found rule with its fixed metadata, supported patterns,
     * recommended remediation steps, and base score.
     */
    public FileNotFoundRule() {
        super(
                "file-not-found-rule",
                ErrorCategory.FILE_NOT_FOUND,
                Severity.MEDIUM,
                "The application tried to access a file or path that does not exist at runtime, is misspelled, or is not available in the expected working directory or mounted volume.",
                patterns(),
                List.of(
                        "Verify the full file path and whether it exists in the runtime environment.",
                        "Check relative path assumptions, working directory, and container volume mounts.",
                        "Review file packaging, build output, and deployment artifact contents.",
                        "Confirm the application has access to the correct location and filename."
                ),
                0.66
        );
    }

    /**
     * Defines the file-not-found-related patterns supported by this rule.
     *
     * <p>The returned map preserves insertion order so pattern evaluation remains
     * deterministic when the parent rule processes them.</p>
     *
     * @return ordered map of human-readable pattern labels and their regex expressions
     */
    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("FileNotFoundException", "filenotfoundexception");
        patterns.put("no such file or directory", "no\\s+such\\s+file\\s+or\\s+directory");
        patterns.put("system cannot find the file specified", "system\\s+cannot\\s+find\\s+the\\s+file\\s+specified");
        patterns.put("path does not exist", "path.*does\\s+not\\s+exist");
        return patterns;
    }
}