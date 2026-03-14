package com.victorlopez.gibfqualitymonitor.core.rules.impl;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

public class CountryPresentRule implements QualityRule {

    private static final String RULE_ID = "COUNTRY_PRESENT";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (occurrence.getCountryCode() == null || occurrence.getCountryCode().isBlank()) {
            return RuleResult.fail(RULE_ID, "Country code is missing or blank");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
