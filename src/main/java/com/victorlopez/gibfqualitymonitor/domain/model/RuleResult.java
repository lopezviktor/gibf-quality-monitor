package com.victorlopez.gibfqualitymonitor.domain.model;

import lombok.Getter;

@Getter
public class RuleResult {

    private final String ruleId;
    private final boolean passed;
    private final String failureReason;

    private RuleResult(String ruleId, boolean passed, String failureReason) {
        this.ruleId = ruleId;
        this.passed = passed;
        this.failureReason = failureReason;
    }

    public static RuleResult pass(String ruleId) {
        return new RuleResult(ruleId, true, null);
    }

    public static RuleResult fail(String ruleId, String reason) {
        return new RuleResult(ruleId, false, reason);
    }
}
