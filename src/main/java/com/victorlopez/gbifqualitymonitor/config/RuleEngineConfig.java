package com.victorlopez.gbifqualitymonitor.config;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.core.rules.RuleEngine;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.BasisOfRecordPresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.CoordinatesPresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.CountryPresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.EventDatePresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.HasMediaRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.NoGeospatialIssuesRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.NoTaxonomyIssuesRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.NoTemporalIssuesRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.RecordedByPresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.TaxonRankAtSpeciesLevelRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine() {
        List<QualityRule> rules = List.of(
                new CoordinatesPresentRule(),
                new EventDatePresentRule(),
                new BasisOfRecordPresentRule(),
                new TaxonRankAtSpeciesLevelRule(),
                new CountryPresentRule(),
                new NoGeospatialIssuesRule(),
                new NoTaxonomyIssuesRule(),
                new NoTemporalIssuesRule(),
                new RecordedByPresentRule(),
                new HasMediaRule()
        );
        return new RuleEngine(rules);
    }
}
