package com.victorlopez.gibfqualitymonitor.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QualityMetrics {

    // Geographic dimension
    private final double coordinatesCoverage;
    private final double geospatialIssueRatio;

    // Temporal dimension
    private final double eventDateCoverage;
    private final double temporalIssueRatio;

    // Taxonomic dimension
    private final double taxonRankAtSpeciesLevel;

    // Metadata dimension
    private final double countryCoverage;
    private final double basisOfRecordCoverage;

    // Operational metrics
    private final int totalRecords;
    private final int recordsWithAnyIssue;
}