package com.victorlopez.gibfqualitymonitor.core.metrics;

import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MetricsAggregatorTest {

    private final MetricsAggregator aggregator = new MetricsAggregator();

    // --- Edge cases ---

    @Test
    void shouldReturnAllZerosWhenTotalRecordsIsZero() {
        QualityMetrics metrics = aggregator.aggregate(List.of(), 0);

        assertThat(metrics.getCoordinatesCoverage()).isEqualTo(0.0);
        assertThat(metrics.getEventDateCoverage()).isEqualTo(0.0);
        assertThat(metrics.getTaxonRankAtSpeciesLevel()).isEqualTo(0.0);
        assertThat(metrics.getCountryCoverage()).isEqualTo(0.0);
        assertThat(metrics.getBasisOfRecordCoverage()).isEqualTo(0.0);
        assertThat(metrics.getGeospatialIssueRatio()).isEqualTo(0.0);
        assertThat(metrics.getTemporalIssueRatio()).isEqualTo(0.0);
        assertThat(metrics.getTotalRecords()).isEqualTo(0);
        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(0);
    }

    @Test
    void shouldReturnAllZerosWhenResultsAreEmptyButTotalRecordsIsPositive() {
        QualityMetrics metrics = aggregator.aggregate(List.of(), 5);

        assertThat(metrics.getCoordinatesCoverage()).isEqualTo(0.0);
        assertThat(metrics.getGeospatialIssueRatio()).isEqualTo(0.0);
        assertThat(metrics.getTotalRecords()).isEqualTo(5);
        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(0);
    }

    // --- Coverage metrics (pass / total * 100) ---

    @Test
    void shouldComputeCoordinatesCoverageCorrectly() {
        List<RuleResult> results = List.of(
                RuleResult.pass("COORDINATES_PRESENT"),
                RuleResult.pass("COORDINATES_PRESENT"),
                RuleResult.fail("COORDINATES_PRESENT", "missing")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 3);

        assertThat(metrics.getCoordinatesCoverage()).isCloseTo(66.67, within(0.01));
    }

    @Test
    void shouldComputeEventDateCoverageCorrectly() {
        List<RuleResult> results = List.of(
                RuleResult.pass("EVENT_DATE_PRESENT"),
                RuleResult.fail("EVENT_DATE_PRESENT", "missing")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getEventDateCoverage()).isEqualTo(50.0);
    }

    @Test
    void shouldComputeTaxonRankAtSpeciesLevelCorrectly() {
        List<RuleResult> results = List.of(
                RuleResult.pass("TAXON_RANK_AT_SPECIES_LEVEL"),
                RuleResult.pass("TAXON_RANK_AT_SPECIES_LEVEL"),
                RuleResult.pass("TAXON_RANK_AT_SPECIES_LEVEL"),
                RuleResult.fail("TAXON_RANK_AT_SPECIES_LEVEL", "not species")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 4);

        assertThat(metrics.getTaxonRankAtSpeciesLevel()).isEqualTo(75.0);
    }

    @Test
    void shouldComputeCountryCoverageCorrectly() {
        List<RuleResult> results = List.of(
                RuleResult.pass("COUNTRY_PRESENT"),
                RuleResult.pass("COUNTRY_PRESENT")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getCountryCoverage()).isEqualTo(100.0);
    }

    @Test
    void shouldComputeBasisOfRecordCoverageCorrectly() {
        List<RuleResult> results = List.of(
                RuleResult.fail("BASIS_OF_RECORD_PRESENT", "missing"),
                RuleResult.fail("BASIS_OF_RECORD_PRESENT", "missing")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getBasisOfRecordCoverage()).isEqualTo(0.0);
    }

    // --- Issue ratios (fail / total * 100, i.e. inverse) ---

    @Test
    void shouldComputeGeospatialIssueRatioAsInverseOfPassed() {
        List<RuleResult> results = List.of(
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 4);

        assertThat(metrics.getGeospatialIssueRatio()).isEqualTo(25.0);
    }

    @Test
    void shouldReturnZeroGeospatialIssueRatioWhenAllPass() {
        List<RuleResult> results = List.of(
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getGeospatialIssueRatio()).isEqualTo(0.0);
    }

    @Test
    void shouldReturnHundredGeospatialIssueRatioWhenAllFail() {
        List<RuleResult> results = List.of(
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues"),
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getGeospatialIssueRatio()).isEqualTo(100.0);
    }

    @Test
    void shouldReturnZeroTemporalIssueRatioSinceNoTemporalRuleExists() {
        QualityMetrics metrics = aggregator.aggregate(List.of(), 3);

        assertThat(metrics.getTemporalIssueRatio()).isEqualTo(0.0);
    }

    // --- recordsWithAnyIssue ---

    @Test
    void shouldCountRecordsWithGeospatialIssuesOnly() {
        List<RuleResult> results = List.of(
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues"),
                RuleResult.pass("NO_TAXONOMY_ISSUES"),
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues"),
                RuleResult.pass("NO_TAXONOMY_ISSUES")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(2);
    }

    @Test
    void shouldCountRecordsWithTaxonomyIssuesOnly() {
        List<RuleResult> results = List.of(
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.fail("NO_TAXONOMY_ISSUES", "has issues"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.fail("NO_TAXONOMY_ISSUES", "has issues")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(2);
    }

    @Test
    void shouldCountRecordsWithBothGeospatialAndTaxonomyIssuesInDifferentRecords() {
        // Record 1 fails geo, Record 2 fails taxonomy → 2 affected records
        List<RuleResult> results = List.of(
                RuleResult.fail("NO_GEOSPATIAL_ISSUES", "has issues"),
                RuleResult.pass("NO_TAXONOMY_ISSUES"),
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.fail("NO_TAXONOMY_ISSUES", "has issues")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroRecordsWithAnyIssueWhenAllPass() {
        List<RuleResult> results = List.of(
                RuleResult.pass("NO_GEOSPATIAL_ISSUES"),
                RuleResult.pass("NO_TAXONOMY_ISSUES")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 1);

        assertThat(metrics.getRecordsWithAnyIssue()).isEqualTo(0);
    }

    // --- totalRecords is preserved ---

    @Test
    void shouldPreserveTotalRecordsInOutput() {
        QualityMetrics metrics = aggregator.aggregate(List.of(), 42);

        assertThat(metrics.getTotalRecords()).isEqualTo(42);
    }

    // --- Mixed results across multiple rules ---

    @Test
    void shouldAggregateMultipleRulesFromFlatListIndependently() {
        List<RuleResult> results = List.of(
                RuleResult.pass("COORDINATES_PRESENT"),
                RuleResult.pass("EVENT_DATE_PRESENT"),
                RuleResult.pass("COORDINATES_PRESENT"),
                RuleResult.fail("EVENT_DATE_PRESENT", "missing")
        );

        QualityMetrics metrics = aggregator.aggregate(results, 2);

        assertThat(metrics.getCoordinatesCoverage()).isEqualTo(100.0);
        assertThat(metrics.getEventDateCoverage()).isEqualTo(50.0);
    }
}
