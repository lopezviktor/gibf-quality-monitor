package com.victorlopez.gibfqualitymonitor.core.normalizer;

import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OccurrenceNormalizerTest {

    private final OccurrenceNormalizer normalizer = new OccurrenceNormalizer();

    // ── fixture records matching gbif-occurrences-response.json ──────────────

    private GbifOccurrence record1001() {
        return GbifOccurrence.builder()
                .gbifID("1001")
                .decimalLatitude(40.416775)
                .decimalLongitude(-3.703790)
                .eventDate("2024-06-15")
                .scientificName("Canis lupus Linnaeus, 1758")
                .taxonRank("SPECIES")
                .basisOfRecord("HUMAN_OBSERVATION")
                .countryCode("ES")
                .recordedBy("John Doe")
                .media(List.of())
                .issues(List.of())
                .build();
    }

    private GbifOccurrence record1002() {
        return GbifOccurrence.builder()
                .gbifID("1002")
                .decimalLatitude(null)
                .decimalLongitude(null)
                .eventDate("2024-07-20")
                .scientificName("Canis lupus Linnaeus, 1758")
                .taxonRank("SPECIES")
                .basisOfRecord("HUMAN_OBSERVATION")
                .countryCode("ES")
                .recordedBy("Jane Smith")
                .media(List.of())
                .issues(List.of("ZERO_COORDINATE"))
                .build();
    }

    private GbifOccurrence record1003() {
        return GbifOccurrence.builder()
                .gbifID("1003")
                .decimalLatitude(41.385064)
                .decimalLongitude(2.173403)
                .eventDate(null)
                .scientificName("Canis lupus Linnaeus, 1758")
                .taxonRank("GENUS")
                .basisOfRecord("HUMAN_OBSERVATION")
                .countryCode("ES")
                .recordedBy(null)
                .media(List.of())
                .issues(List.of("TAXON_MATCH_FUZZY"))
                .build();
    }

    private GbifOccurrence record1004() {
        return GbifOccurrence.builder()
                .gbifID("1004")
                .decimalLatitude(37.389092)
                .decimalLongitude(-5.984459)
                .eventDate("2024-08-10")
                .scientificName("Canis lupus Linnaeus, 1758")
                .taxonRank("SPECIES")
                .basisOfRecord(null)
                .countryCode(null)
                .recordedBy("Carlos García")
                .media(List.of(Map.of("type", "StillImage")))
                .issues(List.of("COUNTRY_DERIVED_FROM_COORDINATES"))
                .build();
    }

    private GbifOccurrence record1005() {
        return GbifOccurrence.builder()
                .gbifID("1005")
                .decimalLatitude(43.263012)
                .decimalLongitude(-2.934985)
                .eventDate("")
                .scientificName("Canis lupus Linnaeus, 1758")
                .taxonRank("SPECIES")
                .basisOfRecord("PRESERVED_SPECIMEN")
                .countryCode("ES")
                .recordedBy("María López")
                .media(List.of())
                .issues(List.of("COORDINATE_ROUNDED"))
                .build();
    }

    // ── list size ─────────────────────────────────────────────────────────────

    @Test
    void shouldReturnOneNormalizedOccurrencePerGbifOccurrence() {
        List<GbifOccurrence> input = List.of(record1001(), record1002(), record1003(), record1004(), record1005());

        List<NormalizedOccurrence> result = normalizer.normalize(input);

        assertThat(result).hasSize(5);
    }

    @Test
    void shouldReturnEmptyListWhenInputIsEmpty() {
        List<NormalizedOccurrence> result = normalizer.normalize(List.of());

        assertThat(result).isEmpty();
    }

    // ── record 1001: all fields present, no issues, no media ─────────────────

    @Test
    void shouldMapAllFieldsCorrectlyForRecord1001() {
        NormalizedOccurrence result = normalizer.normalize(List.of(record1001())).get(0);

        assertThat(result.getGbifId()).isEqualTo("1001");
        assertThat(result.getDecimalLatitude()).isEqualTo(40.416775);
        assertThat(result.getDecimalLongitude()).isEqualTo(-3.703790);
        assertThat(result.getEventDate()).isEqualTo("2024-06-15");
        assertThat(result.getScientificName()).isEqualTo("Canis lupus Linnaeus, 1758");
        assertThat(result.getTaxonRank()).isEqualTo("SPECIES");
        assertThat(result.getBasisOfRecord()).isEqualTo("HUMAN_OBSERVATION");
        assertThat(result.getCountryCode()).isEqualTo("ES");
        assertThat(result.getRecordedBy()).isEqualTo("John Doe");
        assertThat(result.isHasMedia()).isFalse();
        assertThat(result.getIssues()).isEmpty();
    }

    // ── record 1002: null coordinates, has GBIF issues ────────────────────────

    @Test
    void shouldMapNullCoordinatesForRecord1002() {
        NormalizedOccurrence result = normalizer.normalize(List.of(record1002())).get(0);

        assertThat(result.getGbifId()).isEqualTo("1002");
        assertThat(result.getDecimalLatitude()).isNull();
        assertThat(result.getDecimalLongitude()).isNull();
        assertThat(result.getIssues()).containsExactly("ZERO_COORDINATE");
        assertThat(result.isHasMedia()).isFalse();
    }

    // ── record 1003: null eventDate, null recordedBy, GENUS rank ─────────────

    @Test
    void shouldMapNullFieldsForRecord1003() {
        NormalizedOccurrence result = normalizer.normalize(List.of(record1003())).get(0);

        assertThat(result.getGbifId()).isEqualTo("1003");
        assertThat(result.getEventDate()).isNull();
        assertThat(result.getTaxonRank()).isEqualTo("GENUS");
        assertThat(result.getRecordedBy()).isNull();
        assertThat(result.getIssues()).containsExactly("TAXON_MATCH_FUZZY");
        assertThat(result.isHasMedia()).isFalse();
    }

    // ── record 1004: null basisOfRecord, null countryCode, has media ─────────

    @Test
    void shouldMapNullBasisAndCountryAndDetectMediaForRecord1004() {
        NormalizedOccurrence result = normalizer.normalize(List.of(record1004())).get(0);

        assertThat(result.getGbifId()).isEqualTo("1004");
        assertThat(result.getBasisOfRecord()).isNull();
        assertThat(result.getCountryCode()).isNull();
        assertThat(result.isHasMedia()).isTrue();
        assertThat(result.getIssues()).containsExactly("COUNTRY_DERIVED_FROM_COORDINATES");
    }

    // ── record 1005: empty eventDate, single issue ────────────────────────────

    @Test
    void shouldMapEmptyEventDateForRecord1005() {
        NormalizedOccurrence result = normalizer.normalize(List.of(record1005())).get(0);

        assertThat(result.getGbifId()).isEqualTo("1005");
        assertThat(result.getEventDate()).isEmpty();
        assertThat(result.getBasisOfRecord()).isEqualTo("PRESERVED_SPECIMEN");
        assertThat(result.isHasMedia()).isFalse();
        assertThat(result.getIssues()).containsExactly("COORDINATE_ROUNDED");
    }

    // ── hasMedia edge cases ───────────────────────────────────────────────────

    @Test
    void shouldSetHasMediaTrueWhenMediaListIsNonEmpty() {
        GbifOccurrence occurrence = GbifOccurrence.builder()
                .gbifID("x")
                .media(List.of(Map.of("type", "Sound")))
                .issues(List.of())
                .build();

        NormalizedOccurrence result = normalizer.normalize(List.of(occurrence)).get(0);

        assertThat(result.isHasMedia()).isTrue();
    }

    @Test
    void shouldSetHasMediaFalseWhenMediaListIsNull() {
        GbifOccurrence occurrence = GbifOccurrence.builder()
                .gbifID("x")
                .media(null)
                .issues(List.of())
                .build();

        NormalizedOccurrence result = normalizer.normalize(List.of(occurrence)).get(0);

        assertThat(result.isHasMedia()).isFalse();
    }
}
