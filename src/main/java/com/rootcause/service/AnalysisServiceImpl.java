package com.rootcause.service;

import com.rootcause.entity.AnalysisRecordEntity;
import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.mapper.AnalysisRecordMapper;
import com.rootcause.model.*;
import com.rootcause.repository.AnalysisGroupedCountProjection;
import com.rootcause.repository.AnalysisRecordRepository;
import com.rootcause.repository.AnalysisRecordSpecifications;
import com.rootcause.util.ScoreUtils;
import com.rootcause.util.TextNormalizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link AnalysisService}.
 *
 * <p>This service coordinates the main RootCause application flow. It is responsible for:</p>
 *
 * <ol>
 *     <li>validating the incoming raw text</li>
 *     <li>sanitizing and normalizing the input</li>
 *     <li>delegating diagnosis generation to the configured {@link AnalysisEngine}</li>
 *     <li>building the internal {@link AnalysisResult}</li>
 *     <li>persisting the generated analysis</li>
 *     <li>retrieving previously stored analyses</li>
 *     <li>validating pagination and filter parameters for historical queries</li>
 * </ol>
 *
 * <p>The service acts as an application orchestrator and intentionally keeps diagnosis generation
 * delegated to a dedicated engine so that future strategies can evolve without changing the public API.</p>
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final int DEFAULT_PAGE_SIZE_LIMIT = 100;

    private final AnalysisEngine analysisEngine;
    private final AnalysisRecordRepository analysisRecordRepository;
    private final AnalysisRecordMapper analysisRecordMapper;
    private final Clock clock;

    /**
     * Creates the service with all required collaborators.
     *
     * @param analysisEngine engine responsible for producing the diagnostic decision
     * @param analysisRecordRepository repository used to persist and retrieve analysis records
     * @param analysisRecordMapper mapper used to convert between persistence entities and domain models
     * @param clock clock used to generate deterministic analysis timestamps
     */
    public AnalysisServiceImpl(
            final AnalysisEngine analysisEngine,
            final AnalysisRecordRepository analysisRecordRepository,
            final AnalysisRecordMapper analysisRecordMapper,
            final Clock clock
    ) {
        this.analysisEngine = analysisEngine;
        this.analysisRecordRepository = analysisRecordRepository;
        this.analysisRecordMapper = analysisRecordMapper;
        this.clock = clock;
    }

    /**
     * Analyzes the provided raw input text and persists the generated result.
     *
     * <p>The method trims the incoming text, creates a normalized analysis context, delegates the
     * diagnostic decision to the analysis engine, and stores the resulting analysis in the database.</p>
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

        final AnalysisDecision decision = analysisEngine.analyze(context);

        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.now(clock),
                decision.bestMatch().category(),
                decision.bestMatch().severity(),
                decision.bestMatch().probableCause(),
                decision.bestMatch().detectedPatterns(),
                decision.bestMatch().recommendedSteps(),
                ScoreUtils.toBigDecimal(decision.bestMatch().score()),
                decision.bestMatch().ruleCode(),
                sanitizedInput.length(),
                decision.matchedRuleCount()
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
     * Retrieves stored analyses using optional filters and paginated access.
     *
     * <p>The results are always ordered by {@code analyzedAt} descending so the newest
     * analyses appear first.</p>
     *
     * <p>When category or severity filters are provided, their values must match one of the
     * supported enum names exactly. Invalid values are rejected explicitly instead of producing
     * an empty result set.</p>
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param ruleCode optional rule-code filter
     * @param page zero-based page index
     * @param size requested page size
     * @return page of stored analyses matching the provided criteria
     * @throws IllegalArgumentException when pagination is invalid or when category/severity
     *                                  filters do not match supported values
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisResult> getAnalyses(
            final String category,
            final String severity,
            final String ruleCode,
            final int page,
            final int size
    ) {
        validatePagination(page, size);
        validateCategoryFilter(category);
        validateSeverityFilter(severity);

        final Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "analyzedAt")
        );

        final Specification<AnalysisRecordEntity> specification = Specification.allOf(
                AnalysisRecordSpecifications.hasCategory(category),
                AnalysisRecordSpecifications.hasSeverity(severity),
                AnalysisRecordSpecifications.hasRuleCode(ruleCode)
        );

        return analysisRecordRepository.findAll(specification, pageable)
                .map(analysisRecordMapper::toModel);
    }
    /**
     * Converts a grouped count projection into the internal grouped count model.
     *
     * @param projection grouped count projection
     * @return internal grouped count model
     */
    private AnalysisCount toAnalysisCount(final AnalysisGroupedCountProjection projection) {
        return new AnalysisCount(
                projection.getValue(),
                projection.getCount()
        );
    }
    /**
     * Retrieves aggregated statistics for persisted analyses.
     *
     * @return aggregated analysis statistics
     */
    @Override
    @Transactional(readOnly = true)
    public AnalysisStats getAnalysisStats() {
        final long totalAnalyses = analysisRecordRepository.count();

        final List<AnalysisCount> byCategory = analysisRecordRepository.countGroupedByCategory()
                .stream()
                .map(this::toAnalysisCount)
                .toList();

        final List<AnalysisCount> bySeverity = analysisRecordRepository.countGroupedBySeverity()
                .stream()
                .map(this::toAnalysisCount)
                .toList();

        return new AnalysisStats(
                totalAnalyses,
                byCategory,
                bySeverity
        );
    }

    /**
     * Validates pagination parameters accepted by the history endpoint.
     *
     * @param page zero-based page index
     * @param size requested page size
     * @throws IllegalArgumentException when values are outside the accepted range
     */
    private void validatePagination(final int page, final int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        if (size > DEFAULT_PAGE_SIZE_LIMIT) {
            throw new IllegalArgumentException("size must be less than or equal to 100");
        }
    }

    /**
     * Validates the optional category filter used in historical searches.
     *
     * <p>Blank values are treated as absent filters and therefore accepted.</p>
     *
     * @param category optional category filter received from the caller
     * @throws IllegalArgumentException when the provided value does not match any supported
     *                                  {@link ErrorCategory} name
     */
    private void validateCategoryFilter(final String category) {
        if (category == null || category.isBlank()) {
            return;
        }

        try {
            ErrorCategory.valueOf(category);
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "category must be one of: " + getAllowedCategory()
            );
        }
    }

    /**
     * Validates the optional severity filter used in historical searches.
     *
     * <p>Blank values are treated as absent filters and therefore accepted.</p>
     *
     * @param severity optional severity filter received from the caller
     * @throws IllegalArgumentException when the provided value does not match any supported
     *                                  {@link Severity} name
     */
    private void validateSeverityFilter(final String severity) {
        if (severity == null || severity.isBlank()) {
            return;
        }

        try {
            Severity.valueOf(severity);
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "severity must be one of: " + getAllowedSeverity()
            );
        }
    }

    /**
     * Returns the comma-separated list of supported category values accepted by the API.
     *
     * @return supported category names in enum declaration order
     */
    private String getAllowedCategory() {
        return Arrays.stream(ErrorCategory.values())
                .map(ErrorCategory::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the comma-separated list of supported severity values accepted by the API.
     *
     * @return supported severity names in enum declaration order
     */
    private String getAllowedSeverity() {
        return Arrays.stream(Severity.values())
                .map(Severity::name)
                .collect(Collectors.joining(", "));
    }
}