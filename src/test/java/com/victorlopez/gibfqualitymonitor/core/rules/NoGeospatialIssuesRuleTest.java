package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.core.rules.impl.NoGeospatialIssuesRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoGeospatialIssuesRuleTest {

    private final QualityRule rule = new NoGeospatialIssuesRule();

    @Test
    void shouldPassWhenIssuesListIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("NO_GEOSPATIAL_ISSUES");
    }

    @Test
    void shouldPassWhenIssuesContainOnlyNonGeospatialFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .issues(List.of("TAXON_MATCH_FUZZY", "RECORDED_DATE_MISMATCH"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void shouldPassWhenIssuesListIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .issues(null)
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void shouldFailWhenIssuesContainZeroCoordinate() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .issues(List.of("ZERO_COORDINATE"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenIssuesContainCoordinateOutOfRange() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("5")
                .issues(List.of("COORDINATE_OUT_OF_RANGE"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainCoordinateInvalid() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("6")
                .issues(List.of("COORDINATE_INVALID"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainCoordinateRounded() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("7")
                .issues(List.of("COORDINATE_ROUNDED"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainGeodeticDatumInvalid() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("8")
                .issues(List.of("GEODETIC_DATUM_INVALID"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainCountryCoordinateMismatch() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("9")
                .issues(List.of("COUNTRY_COORDINATE_MISMATCH"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainCoordinateReprojectionFailed() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("10")
                .issues(List.of("COORDINATE_REPROJECTION_FAILED"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesMixGeospatialAndNonGeospatialFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("11")
                .issues(List.of("TAXON_MATCH_FUZZY", "COORDINATE_INVALID"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
