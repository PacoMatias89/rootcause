package com.rootcause.service;

import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.AnalysisRequestContext;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.RuleMatch;
import com.rootcause.model.Severity;
import com.rootcause.repository.AnalysisRecordRepository;
import com.rootcause.rules.AnalysisRule;
import com.rootcause.util.ScoreUtils;
import com.rootcause.util.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final List<AnalysisRule> rules;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final Clock clock;

    public AnalysisServiceImpl(
            final List<AnalysisRule> rules,
            final AnalysisRecordRepository analysisRecordRepository,
            final AnalysisRecordMapper analysisRecordMapper,
            final Clock clock
    ) {
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
    public AnalysisResult analyze(final String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text must not be blank");
        }

        final String sanitizedInput = inputText.trim();
        final AnalysisRequestContext context = new AnalysisRequestContext(
                sanitizedInput,
                TextNormalizer.normalize(sanitizedInput)
        );

        final List<RuleMatch> matchedRules = findMatchedRules(context);
        final RuleMatch bestMatch = matchedRules.isEmpty()
                ? buildFallbackMatch()
                : selectBestMatch(matchedRules);

        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.now(clock),
                bestMatch.category(),
                bestMatch.severity(),
                bestMatch.probableCause(),
                bestMatch.detectedPatterns(),
                bestMatch.recommendedSteps(),
                ScoreUtils.toBigDecimal(bestMatch.score()),
                bestMatch.ruleCode(),
                sanitizedInput.length(),
                matchedRules.size()
        );

        analysisRecordRepository.save(analysisRecordMapper.toEntity(sanitizedInput, result));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResult getAnalysisById(final UUID analysisId) {
        return analysisRecordRepository.findById(analysisId)
                .map(analysisRecordMapper::toModel)
                .orElseThrow(() -> new AnalysisNotFoundException(analysisId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResult> getAllAnalyses() {
        return analysisRecordRepository.findAllByOrderByAnalyzedAtDesc()
                .stream()
                .map(analysisRecordMapper::toModel)
                .toList();
    }

    private List<RuleMatch> findMatchedRules(final AnalysisRequestContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(match -> match != null && match.score() > 0.0)
                .toList();
    }

    private RuleMatch selectBestMatch(final List<RuleMatch> matches) {
        return matches.stream()
                .max(Comparator
                        .comparingDouble(RuleMatch::score)
                        .thenComparing(match -> severityPriority(match.severity())))
                .orElseThrow(() -> new IllegalStateException("No rule match available to select"));
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

    private int severityPriority(final Severity severity) {
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}