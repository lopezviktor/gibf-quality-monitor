package com.victorlopez.gibfqualitymonitor.core.normalizer;

import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrence;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OccurrenceNormalizer {

    public List<NormalizedOccurrence> normalize(List<GbifOccurrence> occurrences) {
        return occurrences.stream()
                .map(this::toNormalized)
                .toList();
    }

    private NormalizedOccurrence toNormalized(GbifOccurrence occurrence) {
        return NormalizedOccurrence.builder()
                .gbifId(occurrence.getGbifID())
                .decimalLatitude(occurrence.getDecimalLatitude())
                .decimalLongitude(occurrence.getDecimalLongitude())
                .eventDate(occurrence.getEventDate())
                .scientificName(occurrence.getScientificName())
                .taxonRank(occurrence.getTaxonRank())
                .basisOfRecord(occurrence.getBasisOfRecord())
                .countryCode(occurrence.getCountryCode())
                .recordedBy(occurrence.getRecordedBy())
                .hasMedia(occurrence.getMedia() != null && !occurrence.getMedia().isEmpty())
                .issues(occurrence.getIssues())
                .build();
    }
}
