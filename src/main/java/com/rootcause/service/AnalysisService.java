package com.rootcause.service;

import com.rootcause.model.AnalysisResult;
import com.rootcause.model.AnalysisStats;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;


/**
 * Application service responsible for analyzing raw technical input text.
 *
 * <p>This service defines the main business entry point for the RootCause diagnosis flow.
 * Implementations are responsible for evaluating the input against the configured rules,
 * building the internal analysis result, and coordinating persistence when required.</p>
 */

public interface AnalysisService {
    /**
     * Analyzes the provided technical input and returns the generated diagnosis.
     *
     * @param inputText raw input containing an error message, log excerpt, or stack trace
     * @return generated internal analysis result
     */

    AnalysisResult analyze(String inputText);


    /**
     * Retrieves a previously stored analysis by its identifier.
     *
     * @param analysisId unique identifier of the analysis to retrieve
     * @return stored analysis result
     */

    AnalysisResult getAnalysisById(UUID analysisId);

    /**
     * Retrieves all stored analyses ordered from newest to oldest.
     *
     * @return list of stored analyses
     */

    List<AnalysisResult> getAllAnalyses();
    /**
     * Retrieves stored analyses using optional filters and paginated access.
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param ruleCode optional rule-code filter
     * @param page zero-based page index
     * @param size requested page size
     * @return page of stored analyses that match the provided filters
     */

    Page<AnalysisResult> getAnalyses(String category, String severity, String ruleCode, int page, int size);

    /**
     * Retrieves aggregated statistics for persisted analyses.
     *
     * @return aggregated analysis statistics
     */
    AnalysisStats getAnalysisStats();
}