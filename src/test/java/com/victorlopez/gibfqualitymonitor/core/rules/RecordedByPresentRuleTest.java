package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.core.rules.impl.RecordedByPresentRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecordedByPresentRuleTest {

    private final QualityRule rule = new RecordedByPresentRule();

    @Test
    void shouldPassWhenRecordedByIsPresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .recordedBy("Jane Doe")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("RECORDED_BY_PRESENT");
    }

    @Test
    void shouldFailWhenRecordedByIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .recordedBy(null)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenRecordedByIsBlank() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .recordedBy("   ")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenRecordedByIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .recordedBy("")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
