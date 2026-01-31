package com.dnd.moyeolak.baek;

import com.dnd.moyeolak.global.client.dto.CombinedPlaceResponse;
import com.dnd.moyeolak.global.client.google.GooglePlacesClient;
import com.dnd.moyeolak.global.client.google.dto.NearbySearchRequest;
import com.dnd.moyeolak.global.client.google.dto.TextSearchResponse;
import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;
import com.dnd.moyeolak.global.client.kakao.dto.KeywordSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
public class CombinedPlaceSearchTest {

    @Autowired
    private GooglePlacesClient googlePlacesClient;

    @Autowired
    private KakaoLocalClient kakaoLocalClient;

    // 강남역 좌표
    private static final Double GANGNAM_LAT = 37.497942;
    private static final Double GANGNAM_LNG = 127.027610;

    @Test
    void 구글_카카오_통합_검색_테스트() {
        StopWatch stopWatch = new StopWatch("통합 검색 성능 측정");

        // 1. Google Nearby Search로 강남역 근처 카페 검색 (type: cafe)
        stopWatch.start("통합검색 시작 API 호출");

        NearbySearchRequest googleRequest = NearbySearchRequest.ofDistanceSorted(
                "cafe",
                GANGNAM_LAT,
                GANGNAM_LNG,
                1000.0  // 반경 1km
        );

        TextSearchResponse googleResponse = googlePlacesClient.searchNearby(googleRequest);

        assertThat(googleResponse).isNotNull();
        assertThat(googleResponse.places()).isNotNull();

        System.out.println("=== [1단계] Google Nearby Search 결과: " + googleResponse.places().size() + "건 ===\n");

        List<CombinedPlaceResponse.CombinedPlace> combinedPlaces = new ArrayList<>();

        for (TextSearchResponse.Place googlePlace : googleResponse.places()) {
            String placeName = googlePlace.displayName() != null ? googlePlace.displayName().text() : null;

//            if (placeName == null) {
//                System.out.println("[스킵] 장소명 없음");
//                continue;
//            }

            // Kakao 키워드 검색 (상호명 + 좌표 + 반경)
            KeywordSearchRequest kakaoRequest = KeywordSearchRequest.of(
                    placeName,
                    String.valueOf(GANGNAM_LNG),  // 경도
                    String.valueOf(GANGNAM_LAT),  // 위도
                    100  // 반경 100m (정확도 높이기)
            );

            try {
                CategorySearchResponse kakaoResponse = kakaoLocalClient.searchByKeyword(kakaoRequest);

                if (kakaoResponse.documents() == null || kakaoResponse.documents().isEmpty()) {
                    System.out.println("[스킵] Kakao에서 찾을 수 없음: " + placeName);
                    continue;
                }

                // 첫 번째 결과 사용 (가장 가까운 장소) => 이게 맞나 모르겠네? 정확도순으로 가야하나
                CategorySearchResponse.Place kakaoPlace = kakaoResponse.documents().get(0);

                // 통합 데이터 생성
                CombinedPlaceResponse.CombinedPlace combined = CombinedPlaceResponse.CombinedPlace.of(googlePlace, kakaoPlace);
                combinedPlaces.add(combined);

                System.out.println("[매칭 성공] " + placeName + " → 거리: " + kakaoPlace.distance() + "m");

            } catch (Exception e) {
                System.out.println("[에러] " + placeName + " - " + e.getMessage());
            }
        }

        stopWatch.stop();

        // 3. 최종 통합 결과
        CombinedPlaceResponse finalResponse = new CombinedPlaceResponse(combinedPlaces);

        System.out.println("\n=== [2단계] 최종 통합 결과: " + finalResponse.places().size() + "건 ===\n");

        finalResponse.places().forEach(place -> {
            System.out.println("----------------------------------------");
            System.out.println("[장소명] " + place.name());
            System.out.println("[주소] " + place.formattedAddress());
            System.out.println("[좌표] " + place.latitude() + ", " + place.longitude());

            // Google 데이터
            System.out.println("[Google Maps] " + place.googleMapsUri());
            if (place.editorialSummary() != null) {
                System.out.println("[소개글] " + place.editorialSummary().text());
            }
            if (place.photos() != null && !place.photos().isEmpty()) {
                System.out.println("[사진 수] " + place.photos().size() + "개");
            }

            // Kakao 데이터
            System.out.println("[Kakao 지도] " + place.kakaoPlaceUrl());
            System.out.println("[강남역에서 거리] " + place.distance() + "m");
            System.out.println("[전화번호] " + place.phone());
            System.out.println("[카테고리] " + place.categoryName());
            System.out.println();
        });

        // 검증
        assertThat(finalResponse.places()).isNotEmpty();

        // 성능 측정 결과 출력
        System.out.println("\n=== 성능 측정 결과 ===");
        System.out.println(stopWatch.prettyPrint());
        System.out.println("총 소요 시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }
}
