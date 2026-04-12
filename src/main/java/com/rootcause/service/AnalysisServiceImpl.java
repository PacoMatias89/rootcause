package com.rootcause.service;

import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.*;
import com.rootcause.repository.AnalysisRecordRepository;
import com.rootcause.rules.AnalysisRule;
import com.rootcause.util.ScoreUtils;
import com.rootcause.util.TextNormalizer;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.rootcause.model.Severity.CRITICAL;
import static com.rootcause.model.Severity.HIGH;

public class AnalysisServiceImpl implements AnalysisService {
    private final List<AnalysisRule> rules;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final Clock clock;

    public AnalysisServiceImpl(List<AnalysisRule> rules,
                               AnalysisRecordRepository analysisRecordRepository,
                               AnalysisRecordMapper analysisRecordMapper,
                               Clock clock) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("At least one analysis rule must be configured");
        }

        this.rules = List.copyOf(rules);
        this.analysisRecordRepository = analysisRecordRepository;
        this.analysisRecordMapper = analysisRecordMapper;
        this.clock = clock;
    }
    @Override
    @Transactional
    public AnalysisResult analyze(String inputText) {
        if(inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text must not be blank");
        }

        String sanitizedInput = inputText.trim();
        AnalysisRequestContext context = new AnalysisRequestContext(
                sanitizedInput,
                TextNormalizer.normalize(sanitizedInput)
        );

        RuleMatch bestMatch = selectBestMatch(context);
        AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.now(clock),
                bestMatch.category(),
                bestMatch.severity(),
                bestMatch.probableCause(),
                bestMatch.detectedPatterns(),
                bestMatch.recommendedSteps(),
                ScoreUtils.toBigDecimal(bestMatch.score())
        );

        analysisRecordRepository.save(analysisRecordMapper.toEntity(sanitizedInput, result));

        return result;
    }


    private RuleMatch selectBestMatch(AnalysisRequestContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(match -> match.score() > 0.0)
                .max(Comparator
                        .comparingDouble(RuleMatch::score)
                        .thenComparing(match -> severityPriority(match.severity())))
                .orElseGet(this::buildFallbackMatch);
    }

    private RuleMatch buildFallbackMatch() {
        return new RuleMatch(
                "unknown-fallback-rule",
                ErrorCategory.UNKNOWN,
                Severity.LOW,
                0.15,
                "The input does not match any known rule strongly enough. This may be a new failure pattern or the provided text may be too short or too incomplete.",
                List.of("no strong rule match"),
                List.of(
                        "Provide the full stack trace or a larger log excerpt.",
                        "Include the first exception line and the most relevant caused-by section.",
                        "Add technical context such as service name, environment, recent change, and timestamp."
                )
        );
    }

    private int severityPriority(Severity severity) {
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

}
