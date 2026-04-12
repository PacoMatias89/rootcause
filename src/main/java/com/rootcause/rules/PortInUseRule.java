package com.rootcause.rules;

import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
@Order(40)
public class PortInUseRule extends BasePatternRule {

    public PortInUseRule() {
        super(
                "port-in-use-rule",
                ErrorCategory.PORT_IN_USE,
                Severity.HIGH,
                "The application is trying to bind to a port that is already occupied by another process or another instance of the same service.",
                patterns(),
                List.of(
                        "Identify the process currently using the port.",
                        "Stop the conflicting process or change the application port.",
                        "Check whether another instance of the same service is already running.",
                        "Review local startup scripts, container mappings, and orchestrator port assignments."
                ),
                0.74
        );
    }

    private static LinkedHashMap<String, String> patterns() {
        LinkedHashMap<String, String> patterns = new LinkedHashMap<>();
        patterns.put("address already in use", "address\\s+already\\s+in\\s+use");
        patterns.put("bind exception", "bindexception");
        patterns.put("port already used", "port\\s+\\d+.*already.*use");
        patterns.put("failed to bind", "failed\\s+to\\s+bind");
        return patterns;
    }
}