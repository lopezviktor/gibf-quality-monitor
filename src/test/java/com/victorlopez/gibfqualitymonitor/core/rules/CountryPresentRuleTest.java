package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.core.rules.impl.CountryPresentRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountryPresentRuleTest {

    private final QualityRule rule = new CountryPresentRule();

    @Test
    void shouldPassWhenCountryCodeIsPresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .countryCode("ES")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("COUNTRY_PRESENT");
    }

    @Test
    void shouldFailWhenCountryCodeIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .countryCode(null)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenCountryCodeIsBlank() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .countryCode("   ")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenCountryCodeIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .countryCode("")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
