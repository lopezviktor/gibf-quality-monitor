package com.victorlopez.gibfqualitymonitor.core.scoring;

import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gibfqualitymonitor.domain.model.ScoringResult;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    // Dimension weights (must sum to 1.0)
    private static final double GEOGRAPHIC_WEIGHT  = 0.35;
    private static final double TEMPORAL_WEIGHT    = 0.25;
    private static final double TAXONOMIC_WEIGHT   = 0.25;
    private static final double METADATA_WEIGHT    = 0.15;

    // Penalty factor applied to issue ratios within each dimension score
    private static final double ISSUE_PENALTY_FACTOR = 0.10;

    // Grade thresholds (lower bound inclusive)
    private static final double GRADE_A_MIN = 85.0;
    private static final double GRADE_B_MIN = 70.0;
    private static final double GRADE_C_MIN = 50.0;
    private static final double GRADE_D_MIN = 30.0;

    public ScoringResult calculate(QualityMetrics metrics) {
        double geographicScore = Math.max(0.0, metrics.getCoordinatesCoverage()
                * GEOGRAPHIC_WEIGHT
                * (1 - metrics.getGeospatialIssueRatio() * ISSUE_PENALTY_FACTOR));

        double temporalScore = Math.max(0.0, metrics.getEventDateCoverage()
                * TEMPORAL_WEIGHT
                * (1 - metrics.getTemporalIssueRatio() * ISSUE_PENALTY_FACTOR));

        double taxonomicScore = metrics.getTaxonRankAtSpeciesLevel()
                * TAXONOMIC_WEIGHT;

        double metadataScore = average(metrics.getCountryCoverage(), metrics.getBasisOfRecordCoverage())
                * METADATA_WEIGHT;

        double totalScore = geographicScore + temporalScore + taxonomicScore + metadataScore;

        return ScoringResult.builder()
                .score(totalScore)
                .grade(grade(totalScore))
                .geographicScore(geographicScore)
                .temporalScore(temporalScore)
                .taxonomicScore(taxonomicScore)
                .metadataScore(metadataScore)
                .build();
    }

    private String grade(double score) {
        if (score >= GRADE_A_MIN) return "A";
        if (score >= GRADE_B_MIN) return "B";
        if (score >= GRADE_C_MIN) return "C";
        if (score >= GRADE_D_MIN) return "D";
        return "F";
    }

    private double average(double a, double b) {
        return (a + b) / 2.0;
    }
}
