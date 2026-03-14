package com.victorlopez.gibfqualitymonitor.gbif.client;

import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrenceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GbifClient {

    private static final String OCCURRENCE_SEARCH_PATH = "/occurrence/search";

    private final WebClient webClient;

    public GbifClient(@Value("${gbif.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public GbifOccurrenceResponse fetchOccurrences(Long taxonKey, Integer limit, String country) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(OCCURRENCE_SEARCH_PATH)
                            .queryParam("taxonKey", taxonKey)
                            .queryParam("limit", limit);
                    if (country != null) {
                        uriBuilder.queryParam("country", country);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new GbifApiException(
                                        "GBIF API returned HTTP " + response.statusCode().value() + ": " + body))
                )
                .bodyToMono(GbifOccurrenceResponse.class)
                .block();
    }
}
