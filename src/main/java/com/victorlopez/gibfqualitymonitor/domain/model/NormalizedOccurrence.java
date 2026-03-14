package com.victorlopez.gibfqualitymonitor.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NormalizedOccurrence {

    private final String gbifId;

    // Geographic fields
    private final Double decimalLatitude;
    private final Double decimalLongitude;
    private final String countryCode;

    // Temporal fields
    private final String eventDate;

    // Taxonomic fields
    private final String scientificName;
    private final String taxonRank;

    // Metadata fields
    private final String basisOfRecord;
    private final String recordedBy;

    // Media
    private final boolean hasMedia;

    // Quality flags from GBIF
    private final List<String> issues;
}

