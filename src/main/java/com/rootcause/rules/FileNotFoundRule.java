package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(100)
public class FileNotFoundRule extends BasePatternRule {

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

    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("FileNotFoundException", "filenotfoundexception");
        patterns.put("no such file or directory", "no\\s+such\\s+file\\s+or\\s+directory");
        patterns.put("system cannot find the file specified", "system\\s+cannot\\s+find\\s+the\\s+file\\s+specified");
        patterns.put("path does not exist", "path.*does\\s+not\\s+exist");
        return patterns;
    }
}