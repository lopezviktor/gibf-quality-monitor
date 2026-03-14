package com.victorlopez.gibfqualitymonitor.core.rules.impl;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

public class HasMediaRule implements QualityRule {

    private static final String RULE_ID = "HAS_MEDIA";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (!occurrence.isHasMedia()) {
            return RuleResult.fail(RULE_ID, "Occurrence has no associated media");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
