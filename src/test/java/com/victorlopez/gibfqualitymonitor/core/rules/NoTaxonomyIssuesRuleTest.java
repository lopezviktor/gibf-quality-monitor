package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.core.rules.impl.NoTaxonomyIssuesRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoTaxonomyIssuesRuleTest {

    private final QualityRule rule = new NoTaxonomyIssuesRule();

    @Test
    void shouldPassWhenIssuesListIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("NO_TAXONOMY_ISSUES");
    }

    @Test
    void shouldPassWhenIssuesContainOnlyNonTaxonomyFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .issues(List.of("ZERO_COORDINATE", "COORDINATE_ROUNDED"))
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
    void shouldFailWhenIssuesContainTaxonMatchFuzzy() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .issues(List.of("TAXON_MATCH_FUZZY"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenIssuesContainTaxonMatchHigherrank() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("5")
                .issues(List.of("TAXON_MATCH_HIGHERRANK"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainTaxonMatchNone() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("6")
                .issues(List.of("TAXON_MATCH_NONE"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesContainScientificNameIdNotFound() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("7")
                .issues(List.of("SCIENTIFIC_NAME_ID_NOT_FOUND"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenIssuesMixTaxonomyAndNonTaxonomyFlags() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("8")
                .issues(List.of("ZERO_COORDINATE", "TAXON_MATCH_NONE"))
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
