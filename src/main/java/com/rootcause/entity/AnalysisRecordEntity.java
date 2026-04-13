package com.rootcause.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_record")
public class AnalysisRecordEntity {

    @Id
    private UUID id;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "severity", nullable = false, length = 50)
    private String severity;

    @Column(name = "probable_cause", nullable = false, columnDefinition = "TEXT")
    private String probableCause;

    @Column(name = "detected_patterns", nullable = false, columnDefinition = "TEXT")
    private String detectedPatterns;

    @Column(name = "recommended_steps", nullable = false, columnDefinition = "TEXT")
    private String recommendedSteps;

    @Column(name = "confidence", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "raw_input_length", nullable = false)
    private Integer rawInputLength;

    @Column(name = "matched_rule_count", nullable = false)
    private Integer matchedRuleCount;

    @Column(name = "analyzed_at", nullable = false)
    private OffsetDateTime analyzedAt;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(final String inputText) {
        this.inputText = inputText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(final String severity) {
        this.severity = severity;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(final String probableCause) {
        this.probableCause = probableCause;
    }

    public String getDetectedPatterns() {
        return detectedPatterns;
    }

    public void setDetectedPatterns(final String detectedPatterns) {
        this.detectedPatterns = detectedPatterns;
    }

    public String getRecommendedSteps() {
        return recommendedSteps;
    }

    public void setRecommendedSteps(final String recommendedSteps) {
        this.recommendedSteps = recommendedSteps;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(final BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(final String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Integer getRawInputLength() {
        return rawInputLength;
    }

    public void setRawInputLength(final Integer rawInputLength) {
        this.rawInputLength = rawInputLength;
    }

    public Integer getMatchedRuleCount() {
        return matchedRuleCount;
    }

    public void setMatchedRuleCount(final Integer matchedRuleCount) {
        this.matchedRuleCount = matchedRuleCount;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(final OffsetDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}