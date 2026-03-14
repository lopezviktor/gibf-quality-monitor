package com.victorlopez.gibfqualitymonitor.gbif.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GbifOccurrence {

    private String gbifID;
    private Double decimalLatitude;
    private Double decimalLongitude;
    private String eventDate;
    private String scientificName;
    private String taxonRank;
    private String basisOfRecord;
    private String countryCode;
    private String recordedBy;
    private List<Object> media;
    private List<String> issues;
}
