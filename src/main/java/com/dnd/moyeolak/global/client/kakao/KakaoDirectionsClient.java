package com.dnd.moyeolak.global.client.kakao;

import com.dnd.moyeolak.global.client.kakao.config.KakaoDirectionsApiConfig;
import com.dnd.moyeolak.global.client.kakao.dto.KakaoDirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoDirectionsClient {

    private static final String BASE_URL = "https://apis-navi.kakaomobility.com/v1/directions";

    @Qualifier("kakaoDirectionsRestTemplate")
    private final RestTemplate kakaoDirectionsRestTemplate;
    private final KakaoDirectionsApiConfig kakaoDirectionsApiConfig;

    public KakaoDirectionsResponse.Summary requestDrivingRoute(
            double originLat,
            double originLng,
            double destinationLat,
            double destinationLng,
            LocalDateTime departureTime
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("origin", originLng + "," + originLat)
                .queryParam("destination", destinationLng + "," + destinationLat)
                .queryParam("priority", "RECOMMEND")
                .queryParam("car_fuel", "GASOLINE")
                .queryParam("car_type", 1)
                .queryParam("summary", "true");

        if (departureTime != null) {
            long epochSecond = departureTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
            builder.queryParam("departure_time", epochSecond);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoDirectionsApiConfig.getApiKey());

        try {
            ResponseEntity<KakaoDirectionsResponse> response = kakaoDirectionsRestTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoDirectionsResponse.class
            );

            if (response.getBody() == null || response.getBody().routes() == null || response.getBody().routes().isEmpty()) {
                log.warn("Kakao Directions API 응답이 비어있습니다.");
                return null;
            }

            return response.getBody().routes().getFirst().summary();
        } catch (Exception e) {
            log.error("Kakao Directions API 호출 실패: {}", e.getMessage());
            return null;
        }
    }
}
