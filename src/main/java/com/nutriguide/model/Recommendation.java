package com.nutriguide.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Recommendation: Represents an automated dietary insight or guideline produced by the
 * Rule-Based Recommendation Engine, corresponding to the 'recommendation' relation.
 */
public class Recommendation {

    private Integer recId;
    private Integer userId;
    private String message;
    private String recType;
    private LocalDateTime generatedAt;

    public Recommendation() {
        this.recType = "GENERAL";
    }

    public Recommendation(Integer userId, String message, String recType) {
        this.userId = userId;
        this.message = message;
        this.recType = recType != null ? recType : "GENERAL";
        this.generatedAt = LocalDateTime.now();
    }

    public Recommendation(Integer recId, Integer userId, String message, String recType, LocalDateTime generatedAt) {
        this.recId = recId;
        this.userId = userId;
        this.message = message;
        this.recType = recType;
        this.generatedAt = generatedAt;
    }

    public Recommendation(Recommendation other) {
        if (other != null) {
            this.recId = other.recId;
            this.userId = other.userId;
            this.message = other.message;
            this.recType = other.recType;
            this.generatedAt = other.generatedAt;
        }
    }

    public Integer getRecId() {
        return recId;
    }

    public void setRecId(Integer recId) {
        this.recId = recId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecType() {
        return recType;
    }

    public void setRecType(String recType) {
        this.recType = recType;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return Objects.equals(recId, that.recId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recId);
    }

    @Override
    public String toString() {
        return "Recommendation{" +
                "recId=" + recId +
                ", userId=" + userId +
                ", recType='" + recType + '\'' +
                ", message='" + message + '\'' +
                ", generatedAt=" + generatedAt +
                '}';
    }
}
