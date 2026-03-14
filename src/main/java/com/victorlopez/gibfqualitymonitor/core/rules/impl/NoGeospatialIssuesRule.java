package com.victorlopez.gibfqualitymonitor.core.rules.impl;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

import java.util.List;
import java.util.Set;

public class NoGeospatialIssuesRule implements QualityRule {

    private static final String RULE_ID = "NO_GEOSPATIAL_ISSUES";

    private static final Set<String> GEOSPATIAL_FLAGS = Set.of(
            "ZERO_COORDINATE",
            "COORDINATE_OUT_OF_RANGE",
            "COORDINATE_INVALID",
            "COORDINATE_ROUNDED",
            "GEODETIC_DATUM_INVALID",
            "COUNTRY_COORDINATE_MISMATCH",
            "COORDINATE_REPROJECTION_FAILED"
    );

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        List<String> issues = occurrence.getIssues();
        if (issues == null) {
            return RuleResult.pass(RULE_ID);
        }
        boolean hasGeospatialIssue = issues.stream().anyMatch(GEOSPATIAL_FLAGS::contains);
        if (hasGeospatialIssue) {
            return RuleResult.fail(RULE_ID, "Occurrence has one or more geospatial issues");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
