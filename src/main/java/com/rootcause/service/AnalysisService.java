package com.rootcause.service;

import com.rootcause.model.AnalysisResult;

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

    AnalysisResult getAnalysisById(UUID analysisId);

    List<AnalysisResult> getAllAnalyses();
}