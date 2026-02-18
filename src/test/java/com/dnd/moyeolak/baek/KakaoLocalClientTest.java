package com.dnd.moyeolak.baek;

import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
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
    private KakaoLocalClient kakaoLocalClient;

    @Test
    void 좌표_기반_정확한_장소_검색_테스트() {
        // given - 강남역 근처 "스타벅스" 검색 (확실히 존재하는 장소)
        String placeName = "스타벅스";
        String longitude = "127.027610";  // 강남역 경도
        String latitude = "37.497942";    // 강남역 위도
        int radius = 300;

        KeywordSearchRequest request = KeywordSearchRequest.of(placeName, longitude, latitude, radius);

        // when
        CategorySearchResponse response = kakaoLocalClient.searchByKeyword(request);

        // then - 디버깅용 출력
        System.out.println("=== 응답 결과: " + response.meta().totalCount() + "건 ===");
        if (response.documents() != null) {
            response.documents().forEach(doc ->
                    System.out.println("  - " + doc.placeName() + " | " + doc.placeUrl() + " | " + doc.distance() + "m"));
        }

        assertThat(response).isNotNull();
        assertThat(response.documents()).isNotEmpty();
    }
}
