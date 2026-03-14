package com.victorlopez.gibfqualitymonitor.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class AnalysisReport {

    private final UUID id;
    private final Long taxonKey;
    private final String scientificName;
    private final String country;
    private final Integer requestedLimit;
    private final Integer recordsAnalyzed;
    private final Integer returnedByGbif;
    private final Double completenessScore;
    private final String scoreGrade;
    private final Map<String, Double> metrics;
    private final List<String> recommendations;
    private final Map<String, Double> scoreBreakdown;
    private final LocalDateTime createdAt;
}
