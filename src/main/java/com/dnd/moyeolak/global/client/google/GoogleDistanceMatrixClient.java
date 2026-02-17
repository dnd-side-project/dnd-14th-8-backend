package com.dnd.moyeolak.global.client.google;

import com.dnd.moyeolak.global.client.google.config.GoogleApiConfig;
import com.dnd.moyeolak.global.client.google.dto.GoogleDistanceMatrixResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GoogleDistanceMatrixClient {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    private final RestTemplate restTemplate;
    private final GoogleApiConfig config;

    public GoogleDistanceMatrixClient(
            @Qualifier("googleRestTemplate") RestTemplate restTemplate,
            GoogleApiConfig config
    ) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * N개 출발지 × M개 도착지의 거리/시간 행렬을 한 번의 API 호출로 조회
     *
     * @param origins       출발지 좌표 목록
     * @param destinations  도착지 좌표 목록
     * @param mode          "transit" 또는 "driving"
     * @param departureTime 출발 시각 (Unix timestamp, 초 단위). null이면 현재 시각 기준
     * @return 거리/시간 행렬 응답
     */
    public GoogleDistanceMatrixResponse calculateDistanceMatrix(
            List<Coordinate> origins,
            List<Coordinate> destinations,
            String mode,
            Long departureTime
    ) {
        String originsParam = origins.stream()
                .map(c -> c.latitude() + "," + c.longitude())
                .collect(Collectors.joining("|"));

        String destinationsParam = destinations.stream()
                .map(c -> c.latitude() + "," + c.longitude())
                .collect(Collectors.joining("|"));

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("origins", originsParam)
                .queryParam("destinations", destinationsParam)
                .queryParam("mode", mode)
                .queryParam("language", "ko")
                .queryParam("key", config.getGoogleApiKey());

        if (departureTime != null) {
            uriBuilder.queryParam("departure_time", departureTime);
        }

        String url = uriBuilder.build().toUriString();

        log.debug("Google Distance Matrix API 호출: mode={}, origins={}, destinations={}",
                mode, origins.size(), destinations.size());

        GoogleDistanceMatrixResponse response = restTemplate.getForObject(
                url, GoogleDistanceMatrixResponse.class
        );

        if (response == null || !"OK".equals(response.status())) {
            log.warn("Google Distance Matrix API 실패: status={}",
                    response != null ? response.status() : "null");
            return null;
        }

        log.info("Google Distance Matrix API 성공: {}x{} elements (mode={})",
                origins.size(), destinations.size(), mode);
        return response;
    }

    public record Coordinate(double latitude, double longitude) {}
}
