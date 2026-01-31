package com.dnd.moyeolak.global.client.kakao;

import com.dnd.moyeolak.global.client.kakao.config.KakaoApiConfig;
import com.dnd.moyeolak.global.client.kakao.dto.KakaoLocalResponse;
import com.dnd.moyeolak.test.janghh.dto.response.SubwayStation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class KakaoLocalClient {

    private final RestTemplate kakaoRestTemplate;
    private final KakaoApiConfig kakaoApiConfig;

    private static final String KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final String SUBWAY_CATEGORY_CODE = "SW8";

    public KakaoLocalClient(
        @Qualifier("kakaoRestTemplate") RestTemplate kakaoRestTemplate,
        KakaoApiConfig kakaoApiConfig
    ) {
        this.kakaoRestTemplate = kakaoRestTemplate;
        this.kakaoApiConfig = kakaoApiConfig;
    }

    public List<SubwayStation> findNearestSubwayStations(
        double centerLat,
        double centerLng,
        int radius,
        int limit
    ) {
        String url = String.format(
            "%s?category_group_code=%s&x=%f&y=%f&radius=%d&sort=distance",
            KAKAO_LOCAL_API_URL,
            SUBWAY_CATEGORY_CODE,
            centerLng,
            centerLat,
            radius
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiConfig.getKakaoApiKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoLocalResponse> response = kakaoRestTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoLocalResponse.class
            );

            if (response.getBody() == null) {
                log.warn("Kakao Local API 응답이 비어있습니다.");
                return List.of();
            }

            return response.getBody()
                .documents()
                .stream()
                .limit(limit)
                .map(doc -> new SubwayStation(
                    doc.placeName(),
                    doc.addressName(),
                    doc.roadAddressName(),
                    Double.parseDouble(doc.y()),
                    Double.parseDouble(doc.x()),
                    Integer.parseInt(doc.distance())
                ))
                .toList();

        } catch (Exception e) {
            log.error("Kakao Local API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("지하철역 검색에 실패했습니다.", e);
        }
    }
}
