package com.victorlopez.gibfqualitymonitor.core.metrics;

import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

import java.util.List;

public class MetricsAggregator {

    private static final String COORDINATES_PRESENT        = "COORDINATES_PRESENT";
    private static final String EVENT_DATE_PRESENT         = "EVENT_DATE_PRESENT";
    private static final String TAXON_RANK_AT_SPECIES_LEVEL = "TAXON_RANK_AT_SPECIES_LEVEL";
    private static final String COUNTRY_PRESENT            = "COUNTRY_PRESENT";
    private static final String BASIS_OF_RECORD_PRESENT    = "BASIS_OF_RECORD_PRESENT";
    private static final String NO_GEOSPATIAL_ISSUES       = "NO_GEOSPATIAL_ISSUES";
    private static final String NO_TAXONOMY_ISSUES         = "NO_TAXONOMY_ISSUES";

    public QualityMetrics aggregate(List<RuleResult> results, int totalRecords) {
        if (totalRecords == 0) {
            return QualityMetrics.builder()
                    .coordinatesCoverage(0.0)
                    .geospatialIssueRatio(0.0)
                    .eventDateCoverage(0.0)
                    .temporalIssueRatio(0.0)
                    .taxonRankAtSpeciesLevel(0.0)
                    .countryCoverage(0.0)
                    .basisOfRecordCoverage(0.0)
                    .totalRecords(0)
                    .recordsWithAnyIssue(0)
                    .build();
        }

        long geoFailed = countFailed(results, NO_GEOSPATIAL_ISSUES);
        long taxFailed = countFailed(results, NO_TAXONOMY_ISSUES);

        // recordsWithAnyIssue: upper-bound approximation using the union of geospatial and
        // taxonomy failures. Exact deduplication is not possible from a flat list, so the
        // result is capped at totalRecords to avoid overcounting records that fail both rules.
        int recordsWithAnyIssue = (int) Math.min(totalRecords, geoFailed + taxFailed);

        return QualityMetrics.builder()
                .coordinatesCoverage(percentage(countPassed(results, COORDINATES_PRESENT), totalRecords))
                .geospatialIssueRatio(percentage(geoFailed, totalRecords))
                .eventDateCoverage(percentage(countPassed(results, EVENT_DATE_PRESENT), totalRecords))
                .temporalIssueRatio(0.0) // no temporal issues rule implemented yet
                .taxonRankAtSpeciesLevel(percentage(countPassed(results, TAXON_RANK_AT_SPECIES_LEVEL), totalRecords))
                .countryCoverage(percentage(countPassed(results, COUNTRY_PRESENT), totalRecords))
                .basisOfRecordCoverage(percentage(countPassed(results, BASIS_OF_RECORD_PRESENT), totalRecords))
                .totalRecords(totalRecords)
                .recordsWithAnyIssue(recordsWithAnyIssue)
                .build();
    }

    private long countPassed(List<RuleResult> results, String ruleId) {
        return results.stream()
                .filter(r -> ruleId.equals(r.getRuleId()) && r.isPassed())
                .count();
    }

    private long countFailed(List<RuleResult> results, String ruleId) {
        return results.stream()
                .filter(r -> ruleId.equals(r.getRuleId()) && !r.isPassed())
                .count();
    }

    private double percentage(long count, int total) {
        return (double) count / total * 100.0;
    }
}
