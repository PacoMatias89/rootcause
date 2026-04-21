package com.rootcause.controller;

import com.rootcause.dto.*;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.mapper.AnalysisStatsResponseMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.AnalysisStats;
import com.rootcause.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes the public analysis endpoints of the RootCause API.
 *
 * <p>This controller is responsible for handling HTTP requests related to:</p>
 *
 * <ul>
 *     <li>creating a new analysis from raw technical input</li>
 *     <li>retrieving a previously stored analysis by identifier</li>
 *     <li>retrieving the analysis history as summary records</li>
 * </ul>
 *
 * <p>The controller delegates the business logic to {@link AnalysisService} and uses
 * {@link AnalysisResponseMapper} to transform internal domain models into public API DTOs.</p>
 */
@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisResponseMapper analysisResponseMapper;
    private final AnalysisStatsResponseMapper analysisStatsResponseMapper;

    /**
     * Creates the controller with its required collaborators.
     *
     * @param analysisService application service responsible for analysis operations
     * @param analysisResponseMapper mapper used to convert internal models into response DTOs
     */
    public AnalysisController(
            final AnalysisService analysisService,
            final AnalysisResponseMapper analysisResponseMapper,
            final AnalysisStatsResponseMapper analysisStatsResponseMapper

    ) {
        this.analysisService = analysisService;
        this.analysisResponseMapper = analysisResponseMapper;
        this.analysisStatsResponseMapper = analysisStatsResponseMapper;
    }

    /**
     * Analyzes the provided input text and returns the generated diagnosis.
     *
     * <p>The request body is validated before the business flow is executed. When valid,
     * the controller delegates the analysis to the service layer and maps the internal
     * result to the public {@link AnalyzeResponse} contract.</p>
     *
     * @param request request payload containing the raw input text to analyze
     * @return generated analysis response
     */
    @PostMapping("/analyze")
    @ResponseStatus(HttpStatus.OK)
    public AnalyzeResponse analyze(@Valid @RequestBody final AnalyzeRequest request) {
        return analysisResponseMapper.toAnalyzeResponse(
                analysisService.analyze(request.inputText())
        );
    }

    /**
     * Retrieves a previously stored analysis by its identifier.
     *
     * @param analysisId unique identifier of the analysis to retrieve
     * @return full persisted analysis response
     */
    @GetMapping("/analyses/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AnalyzeResponse getAnalysisById(@PathVariable("id") final UUID analysisId) {
        return analysisResponseMapper.toAnalyzeResponse(
                analysisService.getAnalysisById(analysisId)
        );
    }

    /**
     * Retrieves stored analyses using optional filters and paginated access.
     *
     * <p>Supported optional filters are {@code category}, {@code severity}, {@code ruleCode},
     * {@code analyzedFrom}, {@code analyzedTo}, and {@code search}. Results are always returned
     * ordered from newest to oldest by analysis timestamp.</p>
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param ruleCode optional rule-code filter
     * @param analyzedFrom optional lower bound for the analysis timestamp, inclusive
     * @param analyzedTo optional upper bound for the analysis timestamp, inclusive
     * @param search optional free-text search over persisted textual analysis fields
     * @param page zero-based page index
     * @param size requested page size
     * @return paginated response containing matching analysis summaries
     */
    @GetMapping("/analyses")
    @ResponseStatus(HttpStatus.OK)
    public AnalysisPageResponse getAllAnalyses(
            @RequestParam(value = "category", required = false) final String category,
            @RequestParam(value = "severity", required = false) final String severity,
            @RequestParam(value = "ruleCode", required = false) final String ruleCode,
            @RequestParam(value = "analyzedFrom", required = false) final OffsetDateTime analyzedFrom,
            @RequestParam(value = "analyzedTo", required = false) final OffsetDateTime analyzedTo,
            @RequestParam(value = "search",  required = false) final String search,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = "20") final int size
    ) {
        final Page<AnalysisResult> resultPage = analysisService.getAnalyses(
                category,
                severity,
                ruleCode,
                analyzedFrom,
                analyzedTo,
                search,
                page,
                size
        );

        final List<AnalysisSummaryResponse> items = resultPage.getContent()
                .stream()
                .map(analysisResponseMapper::toSummaryResponse)
                .toList();

        return new AnalysisPageResponse(
                items,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }


    /**
     * Retrieves aggregated statistics for persisted analyses using optional filters.
     *
     * <p>Supported optional filters are {@code category}, {@code severity}, {@code ruleCode},
     * {@code analyzedFrom}, and {@code analyzedTo}. When no filters are provided, the endpoint
     * returns the global statistics for the full persisted history.</p>
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param ruleCode optional rule-code filter
     * @param analyzedFrom optional lower bound for the analysis timestamp, inclusive
     * @param analyzedTo optional upper bound for the analysis timestamp, inclusive
     * @return aggregated analysis statistics response
     */
    @GetMapping("analyses/stats")
    @ResponseStatus(HttpStatus.OK)
    public AnalysisStatsResponse getAnalysisStats(
            @RequestParam(value = "category", required = false) final String category,
            @RequestParam(value = "severity", required = false) final String severity,
            @RequestParam(value = "ruleCode", required = false) final String ruleCode,
            @RequestParam(value = "analyzedFrom", required = false) final OffsetDateTime analyzedFrom,
            @RequestParam(value = "analyzedTo", required = false) final OffsetDateTime analyzedTo
    ){
        final AnalysisStats stats = analysisService.getAnalysisStats(
                category,
                severity,
                ruleCode,
                analyzedFrom,
                analyzedTo
        );

        return analysisStatsResponseMapper.toResponse(stats);
    }
}
