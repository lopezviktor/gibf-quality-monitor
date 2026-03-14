package com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analysis_report")
public class AnalysisReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long taxonKey;
    private String scientificName;
    private String country;
    private Integer requestedLimit;
    private Integer recordsAnalyzed;
    private Integer returnedByGbif;
    private Double completenessScore;
    private String scoreGrade;

    @Column(columnDefinition = "TEXT")
    private String metricsJson;

    @Column(columnDefinition = "TEXT")
    private String recommendationsJson;

    @Column(columnDefinition = "TEXT")
    private String scoreBreakdownJson;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
