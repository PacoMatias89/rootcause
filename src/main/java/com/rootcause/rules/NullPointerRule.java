package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(60)
public class NullPointerRule extends BasePatternRule {

    public NullPointerRule() {
        super(
                "null-pointer-rule",
                ErrorCategory.NULL_POINTER,
                Severity.HIGH,
                "The code is dereferencing a null reference. The probable causes are missing initialization, absent dependency injection, unexpected null input, or an invalid object lifecycle.",
                patterns(),
                List.of(
                        "Inspect the first stack trace line in your own code, not only the framework lines.",
                        "Verify object initialization, dependency injection, and constructor wiring.",
                        "Add null checks or defensive validation at the boundary where the value enters the flow.",
                        "Trace where the null value is produced and why it was not handled earlier."
                ),
                0.78
        );
    }

    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("NullPointerException", "nullpointerexception");
        patterns.put("cannot invoke because is null", "cannot\\s+invoke.*because.*is\\s+null");
        patterns.put("null object reference", "null\\s+object\\s+reference");
        return patterns;
    }
}