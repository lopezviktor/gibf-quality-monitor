package com.victorlopez.gibfqualitymonitor.core.rules.impl;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

import java.util.List;
import java.util.Set;

public class NoTaxonomyIssuesRule implements QualityRule {

    private static final String RULE_ID = "NO_TAXONOMY_ISSUES";

    private static final Set<String> TAXONOMY_FLAGS = Set.of(
            "TAXON_MATCH_FUZZY",
            "TAXON_MATCH_HIGHERRANK",
            "TAXON_MATCH_NONE",
            "SCIENTIFIC_NAME_ID_NOT_FOUND"
    );

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        List<String> issues = occurrence.getIssues();
        if (issues == null) {
            return RuleResult.pass(RULE_ID);
        }
        boolean hasTaxonomyIssue = issues.stream().anyMatch(TAXONOMY_FLAGS::contains);
        if (hasTaxonomyIssue) {
            return RuleResult.fail(RULE_ID, "Occurrence has one or more taxonomy issues");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
