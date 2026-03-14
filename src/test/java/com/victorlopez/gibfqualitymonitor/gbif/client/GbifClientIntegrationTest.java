package com.victorlopez.gibfqualitymonitor.gbif.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrenceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GbifClientIntegrationTest {

    private static final String OCCURRENCE_SEARCH_PATH = "/occurrence/search";
    private static final String FIXTURE_PATH = "wiremock/gbif-occurrences-response.json";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private GbifClient gbifClient;

    @BeforeEach
    void setUp() {
        gbifClient = new GbifClient(wireMock.baseUrl());
    }

    private String loadFixture() throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ── success: 200 with fixture body ────────────────────────────────────────

    @Test
    void shouldReturnFiveRecordsWhenGbifReturnsFixtureResponse() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture())));

        GbifOccurrenceResponse response = gbifClient.fetchOccurrences(5219173L, 5, "ES");

        assertThat(response.getCount()).isEqualTo(5);
        assertThat(response.getResults()).hasSize(5);
        assertThat(response.isEndOfRecords()).isTrue();
    }

    @Test
    void shouldMapGbifIdsCorrectlyFromFixture() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture())));

        GbifOccurrenceResponse response = gbifClient.fetchOccurrences(5219173L, 5, "ES");

        assertThat(response.getResults())
                .extracting("gbifID")
                .containsExactly("1001", "1002", "1003", "1004", "1005");
    }

    @Test
    void shouldOmitCountryParamWhenCountryIsNull() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture())));

        GbifOccurrenceResponse response = gbifClient.fetchOccurrences(5219173L, 5, null);

        assertThat(response.getResults()).hasSize(5);
    }

    // ── error: GBIF returns 500 → GbifApiException ────────────────────────────

    @Test
    void shouldThrowGbifApiExceptionWhenGbifReturns500() {
        wireMock.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> gbifClient.fetchOccurrences(5219173L, 5, "ES"))
                .isInstanceOf(GbifApiException.class)
                .hasMessageContaining("500");
    }
}
