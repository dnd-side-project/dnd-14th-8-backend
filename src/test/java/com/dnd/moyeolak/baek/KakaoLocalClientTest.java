package com.dnd.moyeolak.baek;

import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient2;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchRequest;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;
import com.dnd.moyeolak.global.client.kakao.dto.KeywordSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
public class KakaoLocalClientTest {

    @Autowired
    private KakaoLocalClient2 kakaoLocalClient;

    @Test
    void 키워드로_장소_검색_테스트() {
        // given - "스타벅스 강남" 검색
        KeywordSearchRequest request = KeywordSearchRequest.of("초심스터디카페강남역점");

        // when
        CategorySearchResponse response = kakaoLocalClient.searchByKeyword(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.meta()).isNotNull();
        assertThat(response.documents()).isNotNull();

        System.out.println("=== 키워드 검색 결과: " + response.meta().totalCount() + "건 ===\n");
        response.documents().forEach(place ->
                System.out.println("[" + place.placeName() + "] " + place.addressName()
                        + "\n  - 카테고리: " + place.categoryName()
                        + "\n  - 전화: " + place.phone()
                        + "\n  - 링크: " + place.placeUrl()
                        + "\n")
        );
    }

    @Test
    void 좌표_기반_정확한_장소_검색_테스트() {
        // given - Google Text Search에서 받은 좌표로 Kakao에서 정확한 장소 찾기
        // 예: "공차 강남역점" Google 검색 결과 좌표 (가정)
        String placeName = "초심스터디카페강남역점";
        String longitude = "127.027930";  // Google에서 받은 경도
        String latitude = "37.498095";    // Google에서 받은 위도
        int radius = 50;                   // 50m 반경으로 좁게 검색

        KeywordSearchRequest request = KeywordSearchRequest.of(placeName, longitude, latitude, radius);

        // when
        CategorySearchResponse response = kakaoLocalClient.searchByKeyword(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.documents()).isNotNull();

        System.out.println("=== 좌표 기반 정확한 검색 결과: " + response.meta().totalCount() + "건 ===\n");
        response.documents().forEach(place ->
                System.out.println("[" + place.placeName() + "] " + place.addressName()
                        + "\n  - 카테고리: " + place.categoryName()
                        + "\n  - 전화: " + place.phone()
                        + "\n  - 좌표: (" + place.x() + ", " + place.y() + ")"
                        + "\n  - 거리: " + place.distance() + "m"
                        + "\n  - 링크: " + place.placeUrl()
                        + "\n")
        );
    }

    @Test
    void 카테고리로_카페_검색_테스트() {
        // given - 강남역 좌표 기준 반경 500m 내 카페(CE7) 검색
        CategorySearchRequest request = CategorySearchRequest.of(
                "CE7",           // 카페
                "127.027610",    // 강남역 경도
                "37.497942",     // 강남역 위도
                500             // 반경 500m
        );

        // when
        CategorySearchResponse response = kakaoLocalClient.searchByCategory(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.meta()).isNotNull();
        assertThat(response.documents()).isNotNull();

        System.out.println("총 검색 결과: " + response.meta().totalCount());
        response.documents().forEach(place ->
                System.out.println("[" + place.placeName() + "] " + place.addressName() + " (거리: " + place.distance() + "m)")
        );
    }
}
