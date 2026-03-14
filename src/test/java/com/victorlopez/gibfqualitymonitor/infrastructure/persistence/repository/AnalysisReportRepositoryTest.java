package com.victorlopez.gibfqualitymonitor.infrastructure.persistence.repository;

import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AnalysisReportRepositoryTest {

    @Autowired
    private AnalysisReportRepository repository;

    private AnalysisReportEntity buildEntity() {
        return AnalysisReportEntity.builder()
                .taxonKey(5219173L)
                .scientificName("Canis lupus")
                .country("ES")
                .requestedLimit(300)
                .recordsAnalyzed(287)
                .returnedByGbif(287)
                .completenessScore(74.3)
                .scoreGrade("B")
                .metricsJson("{\"coordinatesCoverage\":91.2}")
                .recommendationsJson("[\"Some recommendation\"]")
                .scoreBreakdownJson("{\"geographicScore\":28.1}")
                .build();
    }

    @Test
    void shouldPersistEntityAndAssignId() {
        AnalysisReportEntity saved = repository.save(buildEntity());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldRetrieveEntityById() {
        AnalysisReportEntity saved = repository.save(buildEntity());

        Optional<AnalysisReportEntity> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
    }

    @Test
    void shouldPersistAllScalarFieldsCorrectly() {
        AnalysisReportEntity saved = repository.save(buildEntity());
        AnalysisReportEntity found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTaxonKey()).isEqualTo(5219173L);
        assertThat(found.getScientificName()).isEqualTo("Canis lupus");
        assertThat(found.getCountry()).isEqualTo("ES");
        assertThat(found.getRequestedLimit()).isEqualTo(300);
        assertThat(found.getRecordsAnalyzed()).isEqualTo(287);
        assertThat(found.getReturnedByGbif()).isEqualTo(287);
        assertThat(found.getCompletenessScore()).isEqualTo(74.3);
        assertThat(found.getScoreGrade()).isEqualTo("B");
    }

    @Test
    void shouldPersistJsonFieldsCorrectly() {
        AnalysisReportEntity saved = repository.save(buildEntity());
        AnalysisReportEntity found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getMetricsJson()).isEqualTo("{\"coordinatesCoverage\":91.2}");
        assertThat(found.getRecommendationsJson()).isEqualTo("[\"Some recommendation\"]");
        assertThat(found.getScoreBreakdownJson()).isEqualTo("{\"geographicScore\":28.1}");
    }

    @Test
    void shouldSetCreatedAtAutomaticallyOnPersist() {
        AnalysisReportEntity saved = repository.save(buildEntity());
        AnalysisReportEntity found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<AnalysisReportEntity> found = repository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldPersistMultipleEntitiesIndependently() {
        AnalysisReportEntity first  = repository.save(buildEntity());
        AnalysisReportEntity second = repository.save(buildEntity());

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(repository.findAll()).hasSize(2);
    }
}
