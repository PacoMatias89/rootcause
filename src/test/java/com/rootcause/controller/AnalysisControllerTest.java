package com.rootcause.controller;

import com.rootcause.exception.AnalysisNotFoundException;
import com.rootcause.exception.GlobalExceptionHandler;
import com.rootcause.mapper.AnalysisResponseMapper;
import com.rootcause.model.AnalysisResult;
import com.rootcause.model.ErrorCategory;
import com.rootcause.model.Severity;
import com.rootcause.service.AnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@Import({GlobalExceptionHandler.class, AnalysisResponseMapper.class})
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    @DisplayName("POST /api/v1/analyze should return analysis response")
    void shouldAnalyzeInputSuccessfully() throws Exception {
        final String inputText = "org.postgresql.util.PSQLException: Connection refused\nFailed to obtain JDBC Connection";

        final AnalysisResult result = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "The application cannot establish a connection to the database.",
                List.of("connection refused", "failed to obtain jdbc connection"),
                List.of("Verify database availability", "Check datasource configuration"),
                new BigDecimal("0.80"),
                "database-connection-rule",
                inputText.length(),
                1
        );

        when(analysisService.analyze(inputText)).thenReturn(result);

        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inputText": "org.postgresql.util.PSQLException: Connection refused\\nFailed to obtain JDBC Connection"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.ruleCode").value("database-connection-rule"))
                .andExpect(jsonPath("$.rawInputLength").value(inputText.length()))
                .andExpect(jsonPath("$.matchedRuleCount").value(1))
                .andExpect(jsonPath("$.confidence").value(0.80));
    }

    @Test
    @DisplayName("GET /api/v1/analyses/{id} should return stored analysis")
    void shouldGetAnalysisByIdSuccessfully() throws Exception {
        final UUID analysisId = UUID.randomUUID();

        final AnalysisResult result = new AnalysisResult(
                analysisId,
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.NULL_POINTER,
                Severity.HIGH,
                "A null value is being dereferenced.",
                List.of("nullpointerexception"),
                List.of("Check null guards"),
                new BigDecimal("0.72"),
                "null-pointer-rule",
                120,
                2
        );

        when(analysisService.getAnalysisById(analysisId)).thenReturn(result);

        mockMvc.perform(get("/api/v1/analyses/{id}", analysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(analysisId.toString()))
                .andExpect(jsonPath("$.category").value("NULL_POINTER"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.ruleCode").value("null-pointer-rule"))
                .andExpect(jsonPath("$.rawInputLength").value(120))
                .andExpect(jsonPath("$.matchedRuleCount").value(2))
                .andExpect(jsonPath("$.confidence").value(0.72));
    }

    @Test
    @DisplayName("GET /api/v1/analyses should return summary list")
    void shouldGetAllAnalysesSuccessfully() throws Exception {
        final AnalysisResult first = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-13T10:15:30Z"),
                ErrorCategory.DATABASE_CONNECTION,
                Severity.CRITICAL,
                "Cannot connect to database.",
                List.of("connection refused"),
                List.of("Verify database availability"),
                new BigDecimal("0.80"),
                "database-connection-rule",
                100,
                1
        );

        final AnalysisResult second = new AnalysisResult(
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-12T08:00:00Z"),
                ErrorCategory.TIMEOUT,
                Severity.HIGH,
                "The operation exceeded the allowed execution time.",
                List.of("timeout"),
                List.of("Review timeout thresholds"),
                new BigDecimal("0.67"),
                "timeout-rule",
                95,
                2
        );

        when(analysisService.getAllAnalyses()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ruleCode").value("database-connection-rule"))
                .andExpect(jsonPath("$[0].category").value("DATABASE_CONNECTION"))
                .andExpect(jsonPath("$[1].ruleCode").value("timeout-rule"))
                .andExpect(jsonPath("$[1].category").value("TIMEOUT"));
    }

    @Test
    @DisplayName("GET /api/v1/analyses/{id} should return 404 when analysis does not exist")
    void shouldReturnNotFoundWhenAnalysisDoesNotExist() throws Exception {
        final UUID analysisId = UUID.randomUUID();

        when(analysisService.getAnalysisById(analysisId))
                .thenThrow(new AnalysisNotFoundException(analysisId));

        mockMvc.perform(get("/api/v1/analyses/{id}", analysisId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Analysis " + analysisId + " not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/analyses/" + analysisId));
    }
}