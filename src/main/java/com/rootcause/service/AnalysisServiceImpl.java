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

/**
 * Default implementation of {@link AnalysisService}.
 *
 * <p>This service coordinates the main RootCause business flow. It is responsible for:</p>
 *
 * <ol>
 *     <li>validating the incoming raw text</li>
 *     <li>sanitizing and normalizing the input</li>
 *     <li>evaluating all configured {@link AnalysisRule} instances</li>
 *     <li>collecting the rules that match with a positive score</li>
 *     <li>selecting the strongest match using score and severity priority</li>
 *     <li>building the internal {@link AnalysisResult}</li>
 *     <li>persisting the generated analysis</li>
 *     <li>retrieving previously stored analyses</li>
 * </ol>
 *
 * <p>When no configured rule matches strongly enough, the service falls back to a predefined
 * low-confidence {@code UNKNOWN} diagnosis.</p>
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final List<AnalysisRule> rules;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final Clock clock;

    /**
     * Creates the service with all required collaborators.
     *
     * @param rules configured analysis rules available in the application context
     * @param analysisRecordRepository repository used to persist and retrieve analysis records
     * @param analysisRecordMapper mapper used to convert between persistence entities and domain models
     * @param clock clock used to generate deterministic analysis timestamps
     * @throws IllegalArgumentException when the rule list is {@code null} or empty
     */
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

    /**
     * Analyzes the provided raw input text and persists the generated result.
     *
     * <p>The method trims the incoming text, creates a normalized analysis context, evaluates
     * all configured rules, keeps the positively matched ones, chooses the best rule, and stores
     * the resulting analysis in the database.</p>
     *
     * <p>The persisted result includes not only the final diagnosis but also metadata useful for
     * historical analysis, such as the matched rule code, the sanitized input length, and the
     * total number of matched rules.</p>
     *
     * @param inputText raw input containing an error message, log excerpt, or stack trace
     * @return generated and persisted analysis result
     * @throws IllegalArgumentException when the input is {@code null} or blank
     */
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

    /**
     * Retrieves a previously persisted analysis by its identifier.
     *
     * @param analysisId unique identifier of the analysis to retrieve
     * @return stored analysis result
     * @throws AnalysisNotFoundException when no analysis exists for the provided identifier
     */
    @Override
    @Transactional(readOnly = true)
    public AnalysisResult getAnalysisById(final UUID analysisId) {
        return analysisRecordRepository.findById(analysisId)
                .map(analysisRecordMapper::toModel)
                .orElseThrow(() -> new AnalysisNotFoundException(analysisId));
    }

    /**
     * Retrieves all persisted analyses ordered from newest to oldest.
     *
     * @return list of stored analysis results sorted by analysis timestamp descending
     */
    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResult> getAllAnalyses() {
        return analysisRecordRepository.findAllByOrderByAnalyzedAtDesc()
                .stream()
                .map(analysisRecordMapper::toModel)
                .toList();
    }

    /**
     * Evaluates all configured rules and keeps only the matches with a positive score.
     *
     * <p>This method also protects against unexpected {@code null} matches returned by any rule.</p>
     *
     * @param context prepared analysis request context
     * @return immutable list of positively matched rules
     */
    private List<RuleMatch> findMatchedRules(final AnalysisRequestContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(match -> match != null && match.score() > 0.0)
                .toList();
    }

    /**
     * Selects the strongest rule from the provided matches.
     *
     * <p>The best match is determined first by highest score and, in case of a tie,
     * by highest severity priority.</p>
     *
     * @param matches non-empty list of positive rule matches
     * @return strongest available rule match
     * @throws IllegalStateException when the list is empty
     */
    private RuleMatch selectBestMatch(final List<RuleMatch> matches) {
        return matches.stream()
                .max(Comparator
                        .comparingDouble(RuleMatch::score)
                        .thenComparing(match -> severityPriority(match.severity())))
                .orElseThrow(() -> new IllegalStateException("No rule match available to select"));
    }

    /**
     * Builds the fallback rule match used when no known rule matches the input strongly enough.
     *
     * @return fallback rule match with {@link ErrorCategory#UNKNOWN}, {@link Severity#LOW},
     * and generic diagnostic recommendations
     */
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

    /**
     * Returns the numeric priority used to break ties between rule matches with the same score.
     *
     * <p>A higher severity implies a higher priority.</p>
     *
     * @param severity severity to rank
     * @return numeric priority for tie-breaking purposes
     */
    private int severityPriority(final Severity severity) {
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}