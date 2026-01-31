package com.dnd.moyeolak.baek;

import com.dnd.moyeolak.global.client.google.GooglePlacesClient;
import com.dnd.moyeolak.global.client.google.dto.NearbySearchRequest;
import com.dnd.moyeolak.global.client.google.dto.TextSearchRequest;
import com.dnd.moyeolak.global.client.google.dto.TextSearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
public class GooglePlacesClientTest {

    @Autowired
    private GooglePlacesClient googlePlacesClient;

    @Test
    void 텍스트로_장소_검색_테스트() {
        // given - "강남역 카페" 검색
        TextSearchRequest request = TextSearchRequest.of("공차 강남역점");

        // when
        TextSearchResponse response = googlePlacesClient.searchText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();

        System.out.println("=== 검색 결과 수: " + response.places().size() + " ===\n");

        response.places().forEach(place -> {
            System.out.println("----------------------------------------");
            System.out.println("[장소명] " + place.displayName().text());
            System.out.println("[주소] " + place.formattedAddress());
            System.out.println("[좌표] 위도: " + place.location().latitude() + ", 경도: " + place.location().longitude());
            System.out.println("[Google Maps] " + place.googleMapsUri());

            // 사진 정보
            if (place.photos() != null && !place.photos().isEmpty()) {
                System.out.println("[사진 수] " + place.photos().size() + "개");
                place.photos().stream().limit(2).forEach(photo ->
                        System.out.println("  - " + photo.name() + " (" + photo.widthPx() + "x" + photo.heightPx() + ")")
                );
            }

            // 영업시간
            if (place.regularOpeningHours() != null) {
                System.out.println("[영업 중] " + (place.regularOpeningHours().openNow() != null && place.regularOpeningHours().openNow() ? "Yes" : "No"));
                if (place.regularOpeningHours().weekdayDescriptions() != null) {
                    System.out.println("[영업시간]");
                    place.regularOpeningHours().weekdayDescriptions().forEach(desc ->
                            System.out.println("  " + desc)
                    );
                }
            }

            // 리뷰
            if (place.reviews() != null && !place.reviews().isEmpty()) {
                System.out.println("[리뷰 수] " + place.reviews().size() + "개");
                place.reviews().stream().limit(2).forEach(review -> {
                    System.out.println("  ★ " + review.rating() + " - " + review.authorAttribution().displayName());
                    if (review.text() != null) {
                        String reviewText = review.text().text();
                        System.out.println("    \"" + (reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText) + "\"");
                    }
                });
            }

            System.out.println();
        });
    }

    @Test
    void 위치_기반_텍스트_검색_테스트() {
        // given - 강남역 좌표 기준 반경 500m 내에서 "카페" 검색
        TextSearchRequest request = TextSearchRequest.of(
                "카페",
                37.497942,   // 강남역 위도
                127.027610,  // 강남역 경도
                500.0        // 반경 500m
        );

        // when
        TextSearchResponse response = googlePlacesClient.searchText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();

        System.out.println("위치 기반 검색 결과 수: " + response.places().size());
        response.places().forEach(place ->
                System.out.println("[" + place.displayName().text() + "] " + place.formattedAddress())
        );
    }

    @Test
    void 결과_개수_제한_검색_테스트() {
        // given - 최대 5개 결과만 요청
        TextSearchRequest request = TextSearchRequest.of("서울 맛집", 5);

        // when
        TextSearchResponse response = googlePlacesClient.searchText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();
        assertThat(response.places().size()).isLessThanOrEqualTo(5);

        System.out.println("제한된 검색 결과 수: " + response.places().size());
        response.places().forEach(place ->
                System.out.println("[" + place.displayName().text() + "] " + place.formattedAddress())
        );
    }

    @Test
    void 거리순_정렬_검색_테스트() {
        // given - 강남역 좌표 기준 반경 1km 내 "카페" 검색 (거리순 정렬)
        TextSearchRequest request = TextSearchRequest.ofDistanceSorted(
                "스터디카페",
                37.497942,   // 강남역 위도
                127.027610,  // 강남역 경도
                1000.0       // 반경 1km
        );

        // when
        TextSearchResponse response = googlePlacesClient.searchText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();

        System.out.println("=== 거리순 정렬 검색 결과: " + response.places().size() + "건 ===");
        System.out.println("(기준: 강남역 37.497942, 127.027610)\n");

        response.places().forEach(place -> {
            double distance = calculateDistance(
                    37.497942, 127.027610,
                    place.location().latitude(), place.location().longitude()
            );
            System.out.printf("[%s] %s%n  - 거리: %.0fm%n  - 좌표: (%.6f, %.6f)%n%n",
                    place.displayName().text(),
                    place.formattedAddress(),
                    distance,
                    place.location().latitude(),
                    place.location().longitude()
            );
        });
    }

    // 두 좌표 사이의 거리 계산 (Haversine formula)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // 지구 반지름 (미터)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Test
    void 카테고리_기반_주변검색_테스트() {
        // given - 강남역 좌표 기준 반경 500m 내 카페 검색 (카테고리: cafe)
        NearbySearchRequest request = NearbySearchRequest.ofDistanceSorted(
                "cafe",
                37.497942,   // 강남역 위도
                127.027610,  // 강남역 경도
                500.0        // 반경 500m
        );

        // when
        TextSearchResponse response = googlePlacesClient.searchNearby(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();

        System.out.println("=== 카테고리(cafe) 주변검색 결과: " + response.places().size() + "건 ===");
        System.out.println("(기준: 강남역 37.497942, 127.027610, 반경 500m)\n");

        response.places().forEach(place -> {
            double distance = calculateDistance(
                    37.497942, 127.027610,
                    place.location().latitude(), place.location().longitude()
            );
            System.out.printf("[%s] %s%n  - 거리: %.0fm%n  - 타입: %s%n%n",
                    place.displayName().text(),
                    place.formattedAddress(),
                    distance,
                    place.types()
            );
        });
    }

    @Test
    void 복수_카테고리_주변검색_테스트() {
        // given - 강남역 좌표 기준 반경 500m 내 카페 + 음식점 검색
        NearbySearchRequest request = NearbySearchRequest.ofDistanceSorted(
                List.of("cafe", "restaurant"),
                37.497942,   // 강남역 위도
                127.027610,  // 강남역 경도
                500.0,       // 반경 500m
                10           // 최대 10개
        );

        // when
        TextSearchResponse response = googlePlacesClient.searchNearby(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.places()).isNotNull();

        System.out.println("=== 복수 카테고리(cafe, restaurant) 주변검색 결과: " + response.places().size() + "건 ===\n");

        response.places().forEach(place -> {
            double distance = calculateDistance(
                    37.497942, 127.027610,
                    place.location().latitude(), place.location().longitude()
            );
            System.out.printf("[%s] %s%n  - 거리: %.0fm%n  - 타입: %s%n%n",
                    place.displayName().text(),
                    place.formattedAddress(),
                    distance,
                    place.types()
            );
        });
    }
}
