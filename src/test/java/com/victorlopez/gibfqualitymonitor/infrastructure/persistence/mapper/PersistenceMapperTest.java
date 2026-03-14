package com.victorlopez.gibfqualitymonitor.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victorlopez.gibfqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceMapperTest {

    private final PersistenceMapper mapper = new PersistenceMapper(new ObjectMapper());

    private AnalysisReport sampleReport() {
        return AnalysisReport.builder()
                .taxonKey(5219173L)
                .scientificName("Canis lupus")
                .country("ES")
                .requestedLimit(300)
                .recordsAnalyzed(300)
                .returnedByGbif(287)
                .completenessScore(74.3)
                .scoreGrade("B")
                .metrics(Map.of(
                        "coordinatesCoverage", 91.2,
                        "eventDateCoverage", 78.4,
                        "taxonRankAtSpeciesLevel", 85.6
                ))
                .recommendations(List.of(
                        "Consider filtering records with coordinates-related issues before spatial use"
                ))
                .scoreBreakdown(Map.of(
                        "geographicScore", 28.1,
                        "temporalScore", 18.4,
                        "taxonomicScore", 19.2,
                        "metadataScore", 8.6
                ))
                .build();
    }

    // ── toEntity ──────────────────────────────────────────────────────────────

    @Test
    void shouldMapScalarFieldsToEntity() {
        AnalysisReport report = sampleReport();

        AnalysisReportEntity entity = mapper.toEntity(report);

        assertThat(entity.getTaxonKey()).isEqualTo(5219173L);
        assertThat(entity.getScientificName()).isEqualTo("Canis lupus");
        assertThat(entity.getCountry()).isEqualTo("ES");
        assertThat(entity.getRequestedLimit()).isEqualTo(300);
        assertThat(entity.getRecordsAnalyzed()).isEqualTo(300);
        assertThat(entity.getReturnedByGbif()).isEqualTo(287);
        assertThat(entity.getCompletenessScore()).isEqualTo(74.3);
        assertThat(entity.getScoreGrade()).isEqualTo("B");
    }

    @Test
    void shouldSerializeMetricsToJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        assertThat(entity.getMetricsJson()).isNotBlank();
        assertThat(entity.getMetricsJson()).contains("coordinatesCoverage");
        assertThat(entity.getMetricsJson()).contains("91.2");
    }

    @Test
    void shouldSerializeRecommendationsToJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        assertThat(entity.getRecommendationsJson()).isNotBlank();
        assertThat(entity.getRecommendationsJson()).contains("filtering records");
    }

    @Test
    void shouldSerializeScoreBreakdownToJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        assertThat(entity.getScoreBreakdownJson()).isNotBlank();
        assertThat(entity.getScoreBreakdownJson()).contains("geographicScore");
        assertThat(entity.getScoreBreakdownJson()).contains("28.1");
    }

    // ── toDomain ─────────────────────────────────────────────────────────────

    @Test
    void shouldDeserializeMetricsFromJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        AnalysisReport result = mapper.toDomain(entity);

        assertThat(result.getMetrics()).containsEntry("coordinatesCoverage", 91.2);
        assertThat(result.getMetrics()).containsEntry("eventDateCoverage", 78.4);
        assertThat(result.getMetrics()).containsEntry("taxonRankAtSpeciesLevel", 85.6);
    }

    @Test
    void shouldDeserializeRecommendationsFromJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        AnalysisReport result = mapper.toDomain(entity);

        assertThat(result.getRecommendations()).hasSize(1);
        assertThat(result.getRecommendations().get(0)).contains("filtering records");
    }

    @Test
    void shouldDeserializeScoreBreakdownFromJson() {
        AnalysisReportEntity entity = mapper.toEntity(sampleReport());

        AnalysisReport result = mapper.toDomain(entity);

        assertThat(result.getScoreBreakdown()).containsEntry("geographicScore", 28.1);
        assertThat(result.getScoreBreakdown()).containsEntry("temporalScore", 18.4);
        assertThat(result.getScoreBreakdown()).containsEntry("taxonomicScore", 19.2);
        assertThat(result.getScoreBreakdown()).containsEntry("metadataScore", 8.6);
    }

    // ── full roundtrip ────────────────────────────────────────────────────────

    @Test
    void shouldPreserveAllScalarFieldsThroughRoundtrip() {
        AnalysisReport original = sampleReport();

        AnalysisReport result = mapper.toDomain(mapper.toEntity(original));

        assertThat(result.getTaxonKey()).isEqualTo(original.getTaxonKey());
        assertThat(result.getScientificName()).isEqualTo(original.getScientificName());
        assertThat(result.getCountry()).isEqualTo(original.getCountry());
        assertThat(result.getRequestedLimit()).isEqualTo(original.getRequestedLimit());
        assertThat(result.getRecordsAnalyzed()).isEqualTo(original.getRecordsAnalyzed());
        assertThat(result.getReturnedByGbif()).isEqualTo(original.getReturnedByGbif());
        assertThat(result.getCompletenessScore()).isEqualTo(original.getCompletenessScore());
        assertThat(result.getScoreGrade()).isEqualTo(original.getScoreGrade());
    }

    @Test
    void shouldPreserveEmptyRecommendationsThroughRoundtrip() {
        AnalysisReport report = AnalysisReport.builder()
                .taxonKey(1L)
                .metrics(Map.of())
                .recommendations(List.of())
                .scoreBreakdown(Map.of())
                .build();

        AnalysisReport result = mapper.toDomain(mapper.toEntity(report));

        assertThat(result.getRecommendations()).isEmpty();
        assertThat(result.getMetrics()).isEmpty();
        assertThat(result.getScoreBreakdown()).isEmpty();
    }
}
