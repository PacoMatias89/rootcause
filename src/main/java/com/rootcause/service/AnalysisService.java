package com.rootcause.service;

import com.rootcause.model.AnalysisResult;

import java.util.List;
import java.util.UUID;

public interface AnalysisService {

    AnalysisResult analyze(String inputText);

    AnalysisResult getAnalysisById(UUID analysisId);

    List<AnalysisResult> getAllAnalyses();
}