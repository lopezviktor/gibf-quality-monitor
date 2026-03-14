package com.victorlopez.gibfqualitymonitor.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victorlopez.gibfqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PersistenceMapper {

    private static final TypeReference<Map<String, Double>> MAP_TYPE  = new TypeReference<>() {};
    private static final TypeReference<List<String>>        LIST_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public PersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AnalysisReportEntity toEntity(AnalysisReport report) {
        return AnalysisReportEntity.builder()
                .taxonKey(report.getTaxonKey())
                .scientificName(report.getScientificName())
                .country(report.getCountry())
                .requestedLimit(report.getRequestedLimit())
                .recordsAnalyzed(report.getRecordsAnalyzed())
                .returnedByGbif(report.getReturnedByGbif())
                .completenessScore(report.getCompletenessScore())
                .scoreGrade(report.getScoreGrade())
                .metricsJson(serialize(report.getMetrics()))
                .recommendationsJson(serialize(report.getRecommendations()))
                .scoreBreakdownJson(serialize(report.getScoreBreakdown()))
                .build();
    }

    public AnalysisReport toDomain(AnalysisReportEntity entity) {
        return AnalysisReport.builder()
                .id(entity.getId())
                .taxonKey(entity.getTaxonKey())
                .scientificName(entity.getScientificName())
                .country(entity.getCountry())
                .requestedLimit(entity.getRequestedLimit())
                .recordsAnalyzed(entity.getRecordsAnalyzed())
                .returnedByGbif(entity.getReturnedByGbif())
                .completenessScore(entity.getCompletenessScore())
                .scoreGrade(entity.getScoreGrade())
                .metrics(deserializeMap(entity.getMetricsJson()))
                .recommendations(deserializeList(entity.getRecommendationsJson()))
                .scoreBreakdown(deserializeMap(entity.getScoreBreakdownJson()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize value to JSON", e);
        }
    }

    private Map<String, Double> deserializeMap(String json) {
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize JSON to Map", e);
        }
    }

    private List<String> deserializeList(String json) {
        try {
            return objectMapper.readValue(json, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize JSON to List", e);
        }
    }
}
