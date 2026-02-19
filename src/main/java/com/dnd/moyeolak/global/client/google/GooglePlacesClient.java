package com.dnd.moyeolak.global.client.google;

import com.dnd.moyeolak.global.client.google.dto.GoogleNearbySearchRequest;
import com.dnd.moyeolak.global.client.google.dto.GoogleTextSearchRequest;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GooglePlacesClient {

    private static final String GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/v1/places";
    private static final String DEFAULT_FIELD_MASK = "places.id,places.displayName,places.formattedAddress" +
            ",places.location,places.regularOpeningHours";

    private final RestClient restClient;

    public GooglePlacesClient(@Value("${google.places.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(GOOGLE_PLACES_BASE_URL)
                .defaultHeader("X-Goog-Api-Key", apiKey)
                .defaultHeader("X-Goog-FieldMask", DEFAULT_FIELD_MASK)
                .build();
    }

    public GooglePlacesResponse searchText(GoogleTextSearchRequest request) {
        return restClient.post()
                .uri(":searchText")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GooglePlacesResponse.class);
    }

    public GooglePlacesResponse searchNearby(GoogleNearbySearchRequest request) {
        return restClient.post()
                .uri(":searchNearby")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GooglePlacesResponse.class);
    }
}
