package com.rootcause.controller;

import com.rootcause.dto.AnalyzeResponse;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import com.rootcause.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @MockitoBean
    private AnalysisResponseMapper analysisResponseMapper;

    @Test
    void shouldReturnAnalysisResponse() throws Exception {
        AnalysisResult result = new AnalysisResult(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                OffsetDateTime.parse("2026-04-12T10:15:30Z"),
                ErrorCategory.PORT_IN_USE,
                Severity.HIGH,
                "The application is trying to bind to a port that is already occupied.",
                List.of("address already in use"),
                List.of("Stop the conflicting process.", "Change the application port."),
                new BigDecimal("0.74")
        );

        AnalyzeResponse response = new AnalyzeResponse(
                result.analysisId(),
                result.analyzedAt(),
                "PORT_IN_USE",
                "HIGH",
                result.probableCause(),
                result.detectedPatterns(),
                result.recommendedSteps(),
                result.confidence()
        );

        when(analysisService.analyze(any(String.class))).thenReturn(result);
        when(analysisResponseMapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "java.net.BindException: Address already in use"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("PORT_IN_USE"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.confidence").value(0.74));
    }

    @Test
    void shouldReturnBadRequestWhenInputTextIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}