package com.rootcause.controller;

import com.rootcause.dto.AnalyzeRequest;
import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisResponseMapper analysisResponseMapper;

    public AnalysisController(AnalysisService analysisService,
                              AnalysisResponseMapper analysisResponseMapper) {
        this.analysisService = analysisService;
        this.analysisResponseMapper = analysisResponseMapper;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@Valid @RequestBody AnalyzeRequest request) {
        AnalysisResult result = analysisService.analyze(request.inputText());
        return ResponseEntity.ok(analysisResponseMapper.toResponse(result));
    }
}