package com.victorlopez.gbifqualitymonitor.core.rules.impl;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

import java.util.List;
import java.util.Set;

public class NoTemporalIssuesRule implements QualityRule {

    private static final String RULE_ID = "NO_TEMPORAL_ISSUES";

    private static final Set<String> TEMPORAL_FLAGS = Set.of(
            "RECORDED_DATE_INVALID",
            "RECORDED_DATE_UNLIKELY",
            "RECORDED_DATE_MISMATCH"
    );

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        List<String> issues = occurrence.getIssues();
        if (issues == null) {
            return RuleResult.pass(RULE_ID);
        }
        boolean hasTemporalIssue = issues.stream().anyMatch(TEMPORAL_FLAGS::contains);
        if (hasTemporalIssue) {
            return RuleResult.fail(RULE_ID, "Occurrence has one or more temporal issues");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
