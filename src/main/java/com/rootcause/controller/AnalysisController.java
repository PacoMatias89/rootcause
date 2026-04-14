package com.rootcause.controller;

import com.rootcause.dto.AnalysisPageResponse;
import com.rootcause.dto.AnalysisSummaryResponse;
import com.rootcause.dto.AnalyzeRequest;
import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Creates the controller with its required collaborators.
     *
     * @param analysisService application service responsible for analysis operations
     * @param analysisResponseMapper mapper used to convert internal models into response DTOs
     */
    public AnalysisController(
            final AnalysisService analysisService,
            final AnalysisResponseMapper analysisResponseMapper
    ) {
        this.analysisService = analysisService;
        this.analysisResponseMapper = analysisResponseMapper;
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
     * <p>Supported optional filters are {@code category}, {@code severity}, and
     * {@code ruleCode}. Results are returned ordered from newest to oldest.</p>
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param ruleCode optional rule-code filter
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
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = "20") final int size
    ) {
        final Page<AnalysisResult> resultPage = analysisService.getAnalyses(
                category,
                severity,
                ruleCode,
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
}
