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
public class GbifOccurrenceResponse {

    private int offset;
    private int limit;
    private boolean endOfRecords;
    private int count;
    private List<GbifOccurrence> results;
}
