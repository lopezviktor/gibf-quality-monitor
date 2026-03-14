# GBIF Data Quality Monitor — Claude Code Context

## Project Overview

Backend REST API that consumes biodiversity occurrence data from the GBIF public API,
applies custom quality rules and weighted scoring, and produces structured quality reports.
The goal is to assess whether a dataset is "fit for purpose" before using it in analysis
or modeling pipelines.

This project is a portfolio piece targeting QA/SDET + backend Java roles.
All decisions must be clean, justified, and defensible in a technical interview.

---

## Tech Stack

- Java 21
- Spring Boot 3.4.x
- Maven
- Spring Data JPA
- H2 (development/tests)
- PostgreSQL (production)
- Lombok
- JUnit 5 + AssertJ
- WireMock (integration tests)

---

## Package Structure

```
com.victorlopez.gibfqualitymonitor
├── api
│   ├── controller
│   ├── dto
│   └── mapper
├── application
│   └── usecase
├── core
│   ├── normalizer
│   ├── rules
│   │   └── impl
│   ├── metrics
│   ├── scoring
│   └── recommendation
├── gbif
│   ├── client
│   ├── dto
│   └── config
├── domain
│   └── model
├── infrastructure
│   └── persistence
│       ├── entity
│       ├── repository
│       └── mapper
└── config
```

---

## Domain Models (already implemented)

### NormalizedOccurrence (domain/model)
Internal model representing a GBIF occurrence record mapped from the external API.
NOT persisted — lives only in memory during analysis pipeline.

```java
@Getter @Builder
public class NormalizedOccurrence {
    private final String gbifId;
    private final Double decimalLatitude;   // nullable
    private final Double decimalLongitude;  // nullable
    private final String countryCode;       // nullable
    private final String eventDate;         // nullable
    private final String scientificName;    // nullable
    private final String taxonRank;         // nullable
    private final String basisOfRecord;     // nullable
    private final String recordedBy;        // nullable
    private final boolean hasMedia;         // derived from media array: !media.isEmpty()
    private final List<String> issues;      // GBIF flags as strings
}
```

### RuleResult (domain/model)
Result of applying one rule to one occurrence record.

```java
@Getter
public class RuleResult {
    private final String ruleId;
    private final boolean passed;
    private final String failureReason;  // null when passed

    public static RuleResult pass(String ruleId) { ... }
    public static RuleResult fail(String ruleId, String reason) { ... }
}
```

### QualityMetrics (domain/model)
Aggregated percentages after applying all rules to all records.

```java
@Getter @Builder
public class QualityMetrics {
    private final double coordinatesCoverage;
    private final double geospatialIssueRatio;
    private final double eventDateCoverage;
    private final double temporalIssueRatio;
    private final double taxonRankAtSpeciesLevel;
    private final double countryCoverage;
    private final double basisOfRecordCoverage;
    private final int totalRecords;
    private final int recordsWithAnyIssue;
}
```

### ScoringResult (domain/model)
Final weighted score and grade.

```java
@Getter @Builder
public class ScoringResult {
    private final double score;
    private final String grade;
    private final double geographicScore;
    private final double temporalScore;
    private final double taxonomicScore;
    private final double metadataScore;
}
```

---

## QualityRule Interface (core/rules)

```java
public interface QualityRule {
    RuleResult evaluate(NormalizedOccurrence occurrence);
    String getRuleId();
}
```

---

## Rules Already Implemented (core/rules/impl)

| Class | RULE_ID | Logic |
|---|---|---|
| CoordinatesPresentRule | COORDINATES_PRESENT | lat != null && lon != null |
| EventDatePresentRule | EVENT_DATE_PRESENT | eventDate != null && !isBlank() |
| BasisOfRecordPresentRule | BASIS_OF_RECORD_PRESENT | basisOfRecord != null && !isBlank() |

Each rule has its own test class in `src/test/.../core/rules/`.

---

## Rules To Implement (TDD — test first, then implementation)

Implement each rule following the exact same pattern as the existing ones.

| Class | RULE_ID | Logic | Edge cases |
|---|---|---|---|
| TaxonRankAtSpeciesLevelRule | TAXON_RANK_AT_SPECIES_LEVEL | taxonRank == "SPECIES" | null, "GENUS", "UNRANKED", "FAMILY" |
| CountryPresentRule | COUNTRY_PRESENT | countryCode != null && !isBlank() | null, blank, empty string |
| NoGeospatialIssuesRule | NO_GEOSPATIAL_ISSUES | issues contains no geospatial flags | empty list, null list, mixed flags |
| NoTaxonomyIssuesRule | NO_TAXONOMY_ISSUES | issues contains no taxonomy flags | empty list, null list, mixed flags |
| RecordedByPresentRule | RECORDED_BY_PRESENT | recordedBy != null && !isBlank() | null, blank, empty string |
| HasMediaRule | HAS_MEDIA | hasMedia == true | false, true |

### Geospatial issue flags to detect (NoGeospatialIssuesRule):
```
ZERO_COORDINATE
COORDINATE_OUT_OF_RANGE
COORDINATE_INVALID
COORDINATE_ROUNDED
GEODETIC_DATUM_INVALID
COUNTRY_COORDINATE_MISMATCH
COORDINATE_REPROJECTION_FAILED
```

### Taxonomy issue flags to detect (NoTaxonomyIssuesRule):
```
TAXON_MATCH_FUZZY
TAXON_MATCH_HIGHERRANK
TAXON_MATCH_NONE
SCIENTIFIC_NAME_ID_NOT_FOUND
```

---

## Scoring Formula

Score is a weighted average across 4 dimensions (total = 100 points):

```
geographicScore  = coordinatesCoverage * 0.35 * (1 - geospatialIssueRatio * 0.10)
temporalScore    = eventDateCoverage   * 0.25 * (1 - temporalIssueRatio   * 0.10)
taxonomicScore   = taxonRankAtSpeciesLevel * 0.25
metadataScore    = average(countryCoverage, basisOfRecordCoverage) * 0.15

totalScore = geographicScore + temporalScore + taxonomicScore + metadataScore
```

Grade scale:
| Score | Grade |
|---|---|
| 85–100 | A |
| 70–84 | B |
| 50–69 | C |
| 30–49 | D |
| 0–29 | F |

---

## Recommendation Rules (RecommendationEngine)

Simple if/then rules based on metric thresholds:

| Condition | Recommendation |
|---|---|
| coordinatesCoverage < 60 | "Limited suitability for map-based analysis due to low coordinate coverage" |
| coordinatesCoverage >= 80 && geospatialIssueRatio < 20 | "Suitable for basic geographic exploration" |
| eventDateCoverage < 60 | "Use caution for temporal analysis because event date coverage is limited" |
| geospatialIssueRatio >= 15 | "Consider filtering records with coordinates-related issues before spatial use" |
| issueRatio >= 30 | "Review records with issues before downstream analysis" |
| taxonRankAtSpeciesLevel >= 95 | "Taxonomic naming coverage is strong for basic occurrence review" |

---

## Analysis Pipeline Flow

```
POST /api/v1/analyses
    → AnalysisController
    → RequestAnalysisUseCase
    → GbifClient (fetch occurrences)
    → OccurrenceNormalizer (GbifOccurrence → NormalizedOccurrence)
    → RuleEngine (apply all rules to all records)
    → MetricsAggregator (List<RuleResult> → QualityMetrics)
    → ScoreCalculator (QualityMetrics → ScoringResult)
    → RecommendationEngine (QualityMetrics → List<String>)
    → AnalysisReportEntity (build + persist)
    → AnalysisReportDTO (return to client)
```

---

## API Endpoints

| Method | Path | Description | Response |
|---|---|---|---|
| POST | /api/v1/analyses | Request new analysis | 201 Created |
| GET | /api/v1/analyses | List reports (paginated) | 200 OK |
| GET | /api/v1/analyses/{id} | Get report by ID | 200 OK / 404 |

### Request DTO
```json
{
  "taxonKey": 5219173,
  "limit": 300,
  "country": "ES"
}
```

### Response DTO
```json
{
  "reportId": "uuid",
  "taxonKey": 5219173,
  "scientificName": "Canis lupus",
  "requestedAt": "2026-03-14T10:00:00Z",
  "recordsAnalyzed": 300,
  "returnedByGbif": 287,
  "completenessScore": 74.3,
  "scoreGrade": "B",
  "scoreBreakdown": {
    "geographicScore": 28.1,
    "temporalScore": 18.4,
    "taxonomicScore": 19.2,
    "metadataScore": 8.6
  },
  "metrics": {
    "coordinatesCoverage": 91.2,
    "eventDateCoverage": 78.4,
    "taxonRankAtSpeciesLevel": 85.6,
    "countryCoverage": 88.9,
    "basisOfRecordCoverage": 100.0,
    "geospatialIssueRatio": 12.3,
    "temporalIssueRatio": 8.1,
    "recordsWithAnyIssue": 71
  },
  "recommendations": [
    "Consider filtering records with coordinates-related issues before spatial use"
  ]
}
```

---

## Persistence

One table: `analysis_report`
- Persists the generated report and request metadata
- Does NOT persist raw GBIF occurrence records
- `metrics`, `scoreBreakdown`, and `recommendations` stored as JSON (TEXT column)

---

## Testing Standards

- TDD approach: test first, implementation second
- Each QualityRule has its own test class
- Tests use AssertJ assertions
- NormalizedOccurrence built with builder pattern in tests
- Minimum test cases per rule: pass, null, blank/empty, edge case specific to that rule

### Key edge cases to always cover:
- Empty string vs blank string vs null
- `(0.0, 0.0)` coordinates — valid technically but suspicious (Gulf of Guinea)
- `issues = []` vs `issues = null`
- `taxonRank = "UNRANKED"` — GBIF uses this explicitly
- GBIF returning 0 records — pipeline must not throw, return score 0

---

## Code Style

- All code, comments, variable names: English
- camelCase for Java
- UPPER_SNAKE_CASE for constants
- Lombok: @Getter, @Builder on domain models
- Private constructors with static factory methods on value objects (RuleResult pattern)
- No magic numbers — use named constants
- SOLID principles, especially Single Responsibility and Open/Closed

---

## Commit Convention

```
feat: add <ClassName> with TDD tests
feat: add core domain models
chore: <configuration or tooling change>
```

---

## What NOT to do

- Do not persist NormalizedOccurrence
- Do not filter occurrences before fetching from GBIF (fetch raw, measure quality)
- Do not add frontend, auth, async processing, or caching in V1
- Do not hardcode taxonKey or any business data
- Do not use magic numbers in scoring — use named constants
