package com.rootcause.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_record")
public class AnalysisRecordEntity {

    @Id
    @Column(name = "id", nullable = false)
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

    @Column(name = "analyzed_at", nullable = false)
    private OffsetDateTime analyzedAt;

    public AnalysisRecordEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    public String getDetectedPatterns() {
        return detectedPatterns;
    }

    public void setDetectedPatterns(String detectedPatterns) {
        this.detectedPatterns = detectedPatterns;
    }

    public String getRecommendedSteps() {
        return recommendedSteps;
    }

    public void setRecommendedSteps(String recommendedSteps) {
        this.recommendedSteps = recommendedSteps;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(OffsetDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}