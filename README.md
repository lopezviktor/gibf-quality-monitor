# GBIF Data Quality Monitor

> Assess the fitness-for-use of biodiversity occurrence data from the GBIF public API — automatically.

![CI](https://github.com/lopezviktor/gibf-quality-monitor/actions/workflows/ci.yml/badge.svg)
![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

---

## What it does

The GBIF Data Quality Monitor fetches biodiversity occurrence records for any taxon from the
[GBIF public API](https://www.gbif.org/developer/occurrence) and runs them through a suite of
configurable quality rules. Each record is evaluated for completeness and data integrity across
four dimensions — geographic, temporal, taxonomic, and metadata. The results are aggregated into
a weighted score (0–100), assigned a letter grade (A–F), and supplemented with plain-language
recommendations about what the dataset is and is not suitable for. Every analysis is persisted and
retrievable via REST.

---

## Architecture

```
HTTP Client
    │
    ▼
┌─────────────────────────────────────────────────────────────────┐
│  API Layer                                                      │
│  AnalysisController  ──►  AnalysisReportMapper  ──►  Response  │
└──────────────┬──────────────────────────────────────────────────┘
               │ execute(taxonKey, limit, country)
               ▼
┌─────────────────────────────────────────────────────────────────┐
│  Application Layer                                              │
│  RequestAnalysisUseCase                                         │
└──┬──────────────────────────────────────────────────────────────┘
   │
   ├─1─► GbifClient ──► GBIF REST API
   │         │
   │         ▼ List<GbifOccurrence>
   │
   ├─2─► OccurrenceNormalizer
   │         │
   │         ▼ List<NormalizedOccurrence>
   │
   ├─3─► RuleEngine  (9 QualityRule implementations)
   │         │
   │         ▼ List<RuleResult>  (n records × 9 rules)
   │
   ├─4─► MetricsAggregator
   │         │
   │         ▼ QualityMetrics  (coverage %, issue ratios)
   │
   ├─5─► ScoreCalculator
   │         │
   │         ▼ ScoringResult  (score, grade, dimension breakdown)
   │
   ├─6─► RecommendationEngine
   │         │
   │         ▼ List<String>  (plain-language recommendations)
   │
   └─7─► PersistenceMapper ──► AnalysisReportRepository ──► DB
```

---

## Quality Rules

All nine rules implement the `QualityRule` interface and are evaluated independently for every
occurrence record. A rule either **passes** or **fails** — there is no partial credit per rule.

| # | Rule ID                    | Passes when                                                                      | Dimension    |
|---|----------------------------|----------------------------------------------------------------------------------|--------------|
| 1 | `COORDINATES_PRESENT`      | `decimalLatitude` and `decimalLongitude` are both non-null                       | Geographic   |
| 2 | `NO_GEOSPATIAL_ISSUES`     | Issues list contains none of: `ZERO_COORDINATE`, `COORDINATE_OUT_OF_RANGE`, `COORDINATE_INVALID`, `COORDINATE_ROUNDED`, `GEODETIC_DATUM_INVALID`, `COUNTRY_COORDINATE_MISMATCH`, `COORDINATE_REPROJECTION_FAILED` | Geographic |
| 3 | `EVENT_DATE_PRESENT`       | `eventDate` is non-null and non-blank                                            | Temporal     |
| 4 | `TAXON_RANK_AT_SPECIES_LEVEL` | `taxonRank` equals `"SPECIES"`                                                | Taxonomic    |
| 5 | `NO_TAXONOMY_ISSUES`       | Issues list contains none of: `TAXON_MATCH_FUZZY`, `TAXON_MATCH_HIGHERRANK`, `TAXON_MATCH_NONE`, `SCIENTIFIC_NAME_ID_NOT_FOUND` | Taxonomic |
| 6 | `COUNTRY_PRESENT`          | `countryCode` is non-null and non-blank                                          | Metadata     |
| 7 | `BASIS_OF_RECORD_PRESENT`  | `basisOfRecord` is non-null and non-blank                                        | Metadata     |
| 8 | `RECORDED_BY_PRESENT`      | `recordedBy` is non-null and non-blank                                           | Informational|
| 9 | `HAS_MEDIA`                | `media` list is non-null and non-empty                                           | Informational|

---

## Scoring

The completeness score is a weighted sum of four dimension scores, each expressed on a 0–100 scale
before weighting.

### Formula

```
Geographic  = max(0, coordinatesCoverage   × 0.35 × (1 − geospatialIssueRatio × 0.10))
Temporal    = max(0, eventDateCoverage     × 0.25 × (1 − temporalIssueRatio   × 0.10))
Taxonomic   =        taxonRankAtSpeciesLevel× 0.25
Metadata    =        avg(countryCoverage, basisOfRecordCoverage) × 0.15

Total Score = Geographic + Temporal + Taxonomic + Metadata   ∈ [0, 100]
```

All coverage values are percentages (0–100). The issue-ratio penalty reduces a dimension score when
a high proportion of records carry flagged issues. Dimension scores are floored at 0 — a severe
issue ratio cannot produce a negative contribution to the total.

### Dimension weights

| Dimension  | Weight | Rationale                                                     |
|------------|--------|---------------------------------------------------------------|
| Geographic | 35%    | Coordinates are essential for the majority of spatial analyses|
| Temporal   | 25%    | Event date underpins time-series and phenology studies        |
| Taxonomic  | 25%    | Species-level resolution is required for most occurrence work |
| Metadata   | 15%    | Country and basis-of-record support filtering and provenance  |

### Grade thresholds

| Grade | Score range | Interpretation                                              |
|-------|-------------|-------------------------------------------------------------|
| **A** | 85 – 100    | High quality — suitable for most analytical use cases       |
| **B** | 70 – 84     | Good quality — minor gaps, suitable for most studies        |
| **C** | 50 – 69     | Moderate quality — review gaps before downstream use        |
| **D** | 30 – 49     | Low quality — significant completeness issues               |
| **F** | 0 – 29      | Poor quality — not recommended for analysis without cleaning |

---

## API Endpoints

| Method | Path                       | Description                                 | Success | Error codes    |
|--------|----------------------------|---------------------------------------------|---------|----------------|
| `POST` | `/api/v1/analyses`         | Run a quality analysis for a taxon          | 201     | 400, 502       |
| `GET`  | `/api/v1/analyses`         | List all past analyses (summary view)       | 200     | —              |
| `GET`  | `/api/v1/analyses/{id}`    | Retrieve a full analysis report by UUID     | 200     | 404            |

`400` is returned when the request body fails validation (`taxonKey` missing, `limit` outside 1–500).
`502` is returned when the GBIF API is unreachable or returns a non-2xx response.

---

## Request / Response Examples

### POST /api/v1/analyses

**Request**

```json
POST /api/v1/analyses
Content-Type: application/json

{
  "taxonKey": 5219173,
  "limit": 100,
  "country": "ES"
}
```

`country` is optional. When omitted, records from all countries are included.

**Response — 201 Created**

```json
{
  "reportId": "a3f1c2d4-89b0-4e5a-b6c7-d8e9f0a1b2c3",
  "taxonKey": 5219173,
  "scientificName": "Canis lupus Linnaeus, 1758",
  "country": "ES",
  "requestedAt": "2025-06-01T12:00:00",
  "recordsAnalyzed": 100,
  "returnedByGbif": 100,
  "completenessScore": 74.32,
  "scoreGrade": "B",
  "scoreBreakdown": {
    "geographicScore": 29.75,
    "temporalScore":   18.20,
    "taxonomicScore":  21.00,
    "metadataScore":    5.37
  },
  "metrics": {
    "coordinatesCoverage":    85.0,
    "geospatialIssueRatio":    6.0,
    "eventDateCoverage":       72.0,
    "temporalIssueRatio":       0.0,
    "taxonRankAtSpeciesLevel": 84.0,
    "countryCoverage":         77.0,
    "basisOfRecordCoverage":   71.0,
    "recordsWithAnyIssue":     14.0
  },
  "recommendations": [
    "Suitable for basic geographic exploration",
    "Use caution for temporal analysis because event date coverage is limited"
  ]
}
```

### GET /api/v1/analyses

**Response — 200 OK**

```json
[
  {
    "reportId": "a3f1c2d4-89b0-4e5a-b6c7-d8e9f0a1b2c3",
    "taxonKey": 5219173,
    "scientificName": "Canis lupus Linnaeus, 1758",
    "requestedAt": "2025-06-01T12:00:00",
    "recordsAnalyzed": 100,
    "completenessScore": 74.32,
    "scoreGrade": "B"
  }
]
```

### GET /api/v1/analyses/{id} — 404 Not Found

```json
(empty body — HTTP 404)
```

---

## Technical Decisions

**Why Spring Boot (WebMVC, not WebFlux)?**
The application orchestrates a single synchronous call to an external API followed by CPU-bound
rule evaluation. There is no benefit to reactive streams here — WebMVC gives simpler controller
code, straightforward exception handling, and familiar testing tooling with MockMvc. WebFlux
is used only where it provides clear value: as the underlying HTTP client (`WebClient`) for the
GBIF call, where its non-blocking I/O and `.onStatus()` DSL make error handling concise.

**Why synchronous processing?**
The POST endpoint is intentionally synchronous: the caller receives the scored report immediately
in the response body rather than polling for a result. Analyses complete in well under a second
for the current limit ceiling (500 records). If the use case grew to bulk imports of millions of
records, an async pipeline with a job queue would be the natural next step.

**Why not persist raw GBIF records?**
Each analysis can return up to 500 records. Storing every occurrence field would balloon the
database, couple the schema to the upstream GBIF model, and add little value — the raw data lives
in GBIF and can be re-fetched. Instead, only the derived report (aggregated metrics, score,
recommendations) is persisted. This keeps storage minimal and the schema stable.

**Why JSON columns for metrics and score breakdown?**
The `metrics` and `scoreBreakdown` maps have a fixed logical structure but could evolve (new rules,
new dimensions). Storing them as JSON in a `TEXT` column avoids a schema migration every time a
rule is added or renamed, while keeping them fully queryable via the Java layer. For structured
reporting at scale, these could be promoted to proper columns or moved to a document store.

**Why a `QualityRule` interface?**
Defining a `QualityRule` interface with a single `evaluate(NormalizedOccurrence)` method makes
each rule independently testable, trivially composable, and open for extension without touching
existing logic. Adding a new rule requires writing one class and registering it in
`RuleEngineConfig` — no other code changes. This is a direct application of the Open/Closed
Principle and the pattern makes the rule set self-documenting in the configuration class.

**Why a separate normalisation step?**
`GbifOccurrence` is shaped by the GBIF API contract and carries Jackson annotations. `NormalizedOccurrence` is a clean domain object with no framework dependencies. The `OccurrenceNormalizer` forms an anti-corruption layer: all rules operate on the domain type, and changes to the GBIF response schema are absorbed in a single place.

---

## Testing Strategy

| Level              | Class(es)                                | Tool                        | Tests |
|--------------------|------------------------------------------|-----------------------------|-------|
| Unit — rules       | `*RuleTest` (9 classes)                  | JUnit 5 + AssertJ           | 43    |
| Unit — engine      | `RuleEngineTest`                         | JUnit 5 + AssertJ           | 6     |
| Unit — metrics     | `MetricsAggregatorTest`                  | JUnit 5 + AssertJ           | 17    |
| Unit — scoring     | `ScoreCalculatorTest`                    | JUnit 5 + AssertJ           | 20    |
| Unit — recommendations | `RecommendationEngineTest`           | JUnit 5 + AssertJ           | 16    |
| Unit — normalizer  | `OccurrenceNormalizerTest`               | JUnit 5 + AssertJ           | 9     |
| Unit — mappers     | `AnalysisReportMapperTest`, `PersistenceMapperTest` | JUnit 5 + AssertJ  | 21    |
| Unit — use case    | `RequestAnalysisUseCaseTest`             | JUnit 5 + Mockito           | 6     |
| Web layer slice    | `AnalysisControllerTest`                 | MockMvc (`@WebMvcTest`)     | 9     |
| HTTP client        | `GbifClientIntegrationTest`              | WireMock standalone         | 4     |
| JPA slice          | `AnalysisReportRepositoryTest`           | H2 (`@DataJpaTest`)         | 7     |
| End-to-end         | `AnalysisFlowIntegrationTest`            | `@SpringBootTest` + WireMock + H2 | 2 |
| Context smoke      | `GibfQualityMonitorApplicationTests`     | `@SpringBootTest`           | 1     |
| **Total**          |                                          |                             | **165** |

The test pyramid is respected: the bulk of coverage lives in fast, focused unit tests; integration
tests verify the seams between layers; and two end-to-end tests confirm the full pipeline under
realistic conditions without hitting the real GBIF API.

---

## Running Locally

### Prerequisites

- Java 21 (e.g. [Eclipse Temurin](https://adoptium.net/))
- Maven 3.9+
- No external database required — H2 in-memory is used by default

### Clone and run

```bash
git clone https://github.com/lopezviktor/gbif-quality-monitor.git
cd gibf-quality-monitor
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Run the test suite

```bash
mvn test
```

### Example curl

```bash
# Analyse Canis lupus occurrences in Spain (up to 50 records)
curl -s -X POST http://localhost:8080/api/v1/analyses \
  -H "Content-Type: application/json" \
  -d '{"taxonKey": 5219173, "limit": 50, "country": "ES"}' | jq .

# List all past analyses
curl -s http://localhost:8080/api/v1/analyses | jq .

# Retrieve a specific report
curl -s http://localhost:8080/api/v1/analyses/<reportId> | jq .
```

### Configuration

| Property                | Default                        | Description                        |
|-------------------------|--------------------------------|------------------------------------|
| `gbif.api.base-url`     | `https://api.gbif.org/v1`      | GBIF API base URL                  |
| `spring.datasource.url` | `jdbc:h2:mem:gbifqualitydb`    | Switch to PostgreSQL for production|

---

## Project Structure

```
src/main/java/com/lopezviktor/gbifqualitymonitor/
│
├── api/
│   ├── controller/         # AnalysisController — REST endpoints (POST, GET ×2)
│   ├── dto/                # Request/response DTOs (AnalysisRequestDTO, AnalysisReportDTO, AnalysisReportSummaryDTO)
│   └── mapper/             # AnalysisReportMapper — domain → DTO conversion
│
├── application/
│   └── usecase/            # RequestAnalysisUseCase, FindAnalysisReportsUseCase — orchestration
│
├── config/                 # JacksonConfig, RuleEngineConfig — Spring bean wiring
│
├── core/
│   ├── metrics/            # MetricsAggregator — RuleResult list → QualityMetrics
│   ├── normalizer/         # OccurrenceNormalizer — GbifOccurrence → NormalizedOccurrence
│   ├── recommendation/     # RecommendationEngine — QualityMetrics → List<String>
│   ├── rules/              # QualityRule interface + RuleEngine orchestrator
│   │   └── impl/           # 9 QualityRule implementations
│   └── scoring/            # ScoreCalculator — QualityMetrics → ScoringResult
│
├── domain/
│   └── model/              # Pure domain objects (NormalizedOccurrence, QualityMetrics, ScoringResult, AnalysisReport, …)
│
├── gbif/
│   ├── client/             # GbifClient (WebClient), GbifApiException
│   └── dto/                # GbifOccurrence, GbifOccurrenceResponse (Jackson-annotated)
│
└── infrastructure/
    └── persistence/
        ├── entity/         # AnalysisReportEntity — JPA entity (@Entity, UUID PK, @PrePersist)
        ├── mapper/         # PersistenceMapper — domain ↔ entity, JSON serialisation via ObjectMapper
        └── repository/     # AnalysisReportRepository — JpaRepository<AnalysisReportEntity, UUID>
```

---

## License

MIT © Victor Lopez
