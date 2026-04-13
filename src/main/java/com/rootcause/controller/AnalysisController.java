package com.rootcause.controller;

import com.rootcause.dto.AnalysisSummaryResponse;
import com.rootcause.dto.AnalyzeRequest;
import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisResponseMapper analysisResponseMapper;

    public AnalysisController(
            final AnalysisService analysisService,
            final AnalysisResponseMapper analysisResponseMapper
    ) {
        this.analysisService = analysisService;
        this.analysisResponseMapper = analysisResponseMapper;
    }

    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.OK)
    public AnalyzeResponse analyze(@Valid @RequestBody final AnalyzeRequest request) {
        return analysisResponseMapper.toAnalyzeResponse(
                analysisService.analyze(request.inputText())
        );
    }

    @GetMapping("/analyses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AnalyzeResponse getAnalysisById(@PathVariable("id") final UUID analysisId) {
        return analysisResponseMapper.toAnalyzeResponse(
                analysisService.getAnalysisById(analysisId)
        );
    }

    @GetMapping("/analyses")
    @ResponseStatus(HttpStatus.OK)
    public List<AnalysisSummaryResponse> getAllAnalyses() {
        return analysisService.getAllAnalyses()
                .stream()
                .map(analysisResponseMapper::toSummaryResponse)
                .toList();
    }
}