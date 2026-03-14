package com.victorlopez.gibfqualitymonitor.core.rules.impl;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

public class RecordedByPresentRule implements QualityRule {

    private static final String RULE_ID = "RECORDED_BY_PRESENT";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (occurrence.getRecordedBy() == null || occurrence.getRecordedBy().isBlank()) {
            return RuleResult.fail(RULE_ID, "Recorded by is missing or blank");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
