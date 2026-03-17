package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.NoTemporalIssuesRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoTemporalIssuesRuleTest {

    private final QualityRule rule = new NoTemporalIssuesRule();

    @Test
    void shouldPassWhenIssuesListIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("NO_TEMPORAL_ISSUES");
    }

    @Test
    void shouldPassWhenIssuesContainOnlyNonTemporalFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .issues(List.of("COORDINATE_INVALID", "TAXON_MATCH_FUZZY"))
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
    void shouldFailWhenIssuesContainRecordedDateInvalid() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .issues(List.of("RECORDED_DATE_INVALID"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenIssuesContainRecordedDateUnlikely() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("5")
                .issues(List.of("RECORDED_DATE_UNLIKELY"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainRecordedDateMismatch() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("6")
                .issues(List.of("RECORDED_DATE_MISMATCH"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesMixTemporalAndNonTemporalFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("7")
                .issues(List.of("COORDINATE_INVALID", "RECORDED_DATE_INVALID"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
