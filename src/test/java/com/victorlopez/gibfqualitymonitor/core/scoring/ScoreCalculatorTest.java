package com.victorlopez.gibfqualitymonitor.core.scoring;

import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gibfqualitymonitor.domain.model.ScoringResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    // ── helpers ─────────────────────────────────────────────────────────────

    /** All coverages equal to the given value, all issue ratios = 0. */
    private QualityMetrics metricsWithEqualCoverages(double coverage) {
        return QualityMetrics.builder()
                .coordinatesCoverage(coverage)
                .geospatialIssueRatio(0.0)
                .eventDateCoverage(coverage)
                .temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(coverage)
                .countryCoverage(coverage)
                .basisOfRecordCoverage(coverage)
                .totalRecords(10)
                .recordsWithAnyIssue(0)
                .build();
    }

    // ── edge cases ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnZeroScoreAndGradeFWhenAllMetricsAreZero() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(0.0));

        assertThat(result.getScore()).isEqualTo(0.0);
        assertThat(result.getGrade()).isEqualTo("F");
        assertThat(result.getGeographicScore()).isEqualTo(0.0);
        assertThat(result.getTemporalScore()).isEqualTo(0.0);
        assertThat(result.getTaxonomicScore()).isEqualTo(0.0);
        assertThat(result.getMetadataScore()).isEqualTo(0.0);
    }

    @Test
    void shouldReturnHundredScoreAndGradeAWhenAllMetricsAreHundredAndNoIssues() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(100.0));

        assertThat(result.getScore()).isCloseTo(100.0, within(0.01));
        assertThat(result.getGrade()).isEqualTo("A");
        assertThat(result.getGeographicScore()).isCloseTo(35.0, within(0.01));
        assertThat(result.getTemporalScore()).isCloseTo(25.0, within(0.01));
        assertThat(result.getTaxonomicScore()).isCloseTo(25.0, within(0.01));
        assertThat(result.getMetadataScore()).isCloseTo(15.0, within(0.01));
    }

    // ── dimension scores ─────────────────────────────────────────────────────

    @Test
    void shouldComputeGeographicScoreCorrectly() {
        // geographicScore = 80 * 0.35 * (1 - 0 * 0.10) = 28.0
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(80.0)
                .geospatialIssueRatio(0.0)
                .eventDateCoverage(0.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getGeographicScore()).isCloseTo(28.0, within(0.01));
    }

    @Test
    void shouldApplyGeospatialIssuePenaltyToGeographicScore() {
        // geographicScore = 100 * 0.35 * (1 - 5 * 0.10) = 35 * 0.5 = 17.5
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(100.0)
                .geospatialIssueRatio(5.0)
                .eventDateCoverage(0.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getGeographicScore()).isCloseTo(17.5, within(0.01));
    }

    @Test
    void shouldComputeTemporalScoreCorrectly() {
        // temporalScore = 60 * 0.25 * (1 - 0 * 0.10) = 15.0
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(0.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(60.0)
                .temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getTemporalScore()).isCloseTo(15.0, within(0.01));
    }

    @Test
    void shouldApplyTemporalIssuePenaltyToTemporalScore() {
        // temporalScore = 100 * 0.25 * (1 - 4 * 0.10) = 25 * 0.6 = 15.0
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(0.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(100.0)
                .temporalIssueRatio(4.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getTemporalScore()).isCloseTo(15.0, within(0.01));
    }

    @Test
    void shouldComputeTaxonomicScoreCorrectly() {
        // taxonomicScore = 80 * 0.25 = 20.0
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(0.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(0.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(80.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getTaxonomicScore()).isCloseTo(20.0, within(0.01));
    }

    @Test
    void shouldComputeMetadataScoreAsAverageOfCountryAndBasisOfRecord() {
        // metadataScore = avg(80, 60) * 0.15 = 70 * 0.15 = 10.5
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(0.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(0.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(80.0)
                .basisOfRecordCoverage(60.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getMetadataScore()).isCloseTo(10.5, within(0.01));
    }

    @Test
    void shouldComputeTotalScoreAsSumOfAllDimensions() {
        // geo=28, temporal=15, taxonomic=20, metadata=10.5 → total=73.5
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(80.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(60.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(80.0)
                .countryCoverage(80.0).basisOfRecordCoverage(60.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getScore()).isCloseTo(73.5, within(0.01));
    }

    // ── grade thresholds ─────────────────────────────────────────────────────

    @Test
    void shouldAssignGradeAWhenScoreIsExactly85() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(85.0));

        assertThat(result.getScore()).isCloseTo(85.0, within(0.01));
        assertThat(result.getGrade()).isEqualTo("A");
    }

    @Test
    void shouldAssignGradeBWhenScoreIsJustBelow85() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(84.0));

        assertThat(result.getGrade()).isEqualTo("B");
    }

    @Test
    void shouldAssignGradeBWhenScoreIsExactly70() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(70.0));

        assertThat(result.getScore()).isCloseTo(70.0, within(0.01));
        assertThat(result.getGrade()).isEqualTo("B");
    }

    @Test
    void shouldAssignGradeCWhenScoreIsJustBelow70() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(69.0));

        assertThat(result.getGrade()).isEqualTo("C");
    }

    @Test
    void shouldAssignGradeCWhenScoreIsExactly50() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(50.0));

        assertThat(result.getScore()).isCloseTo(50.0, within(0.01));
        assertThat(result.getGrade()).isEqualTo("C");
    }

    @Test
    void shouldAssignGradeDWhenScoreIsJustBelow50() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(49.0));

        assertThat(result.getGrade()).isEqualTo("D");
    }

    @Test
    void shouldAssignGradeDWhenScoreIsExactly30() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(30.0));

        assertThat(result.getScore()).isCloseTo(30.0, within(0.01));
        assertThat(result.getGrade()).isEqualTo("D");
    }

    @Test
    void shouldAssignGradeFWhenScoreIsJustBelow30() {
        ScoringResult result = calculator.calculate(metricsWithEqualCoverages(29.0));

        assertThat(result.getGrade()).isEqualTo("F");
    }

    // ── floor at zero ─────────────────────────────────────────────────────────

    @Test
    void shouldFloorGeographicScoreAtZeroWhenIssuePenaltyExceedsBase() {
        // geospatialIssueRatio=15 → (1 - 15*0.10) = -0.5 → without floor: negative
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(100.0)
                .geospatialIssueRatio(15.0)
                .eventDateCoverage(0.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getGeographicScore()).isEqualTo(0.0);
    }

    @Test
    void shouldFloorTemporalScoreAtZeroWhenIssuePenaltyExceedsBase() {
        // temporalIssueRatio=12 → (1 - 12*0.10) = -0.2 → without floor: negative
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(0.0).geospatialIssueRatio(0.0)
                .eventDateCoverage(100.0)
                .temporalIssueRatio(12.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getTemporalScore()).isEqualTo(0.0);
    }

    @Test
    void shouldNotReduceTotalScoreBelowZeroWhenMultipleDimensionsAreFloored() {
        // Both geo and temporal penalty > 10, taxonomic and metadata = 0
        QualityMetrics metrics = QualityMetrics.builder()
                .coordinatesCoverage(100.0)
                .geospatialIssueRatio(20.0)
                .eventDateCoverage(100.0)
                .temporalIssueRatio(20.0)
                .taxonRankAtSpeciesLevel(0.0)
                .countryCoverage(0.0).basisOfRecordCoverage(0.0)
                .totalRecords(10).recordsWithAnyIssue(0)
                .build();

        ScoringResult result = calculator.calculate(metrics);

        assertThat(result.getGeographicScore()).isEqualTo(0.0);
        assertThat(result.getTemporalScore()).isEqualTo(0.0);
        assertThat(result.getScore()).isEqualTo(0.0);
    }
}
