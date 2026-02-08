package com.dnd.moyeolak.global.client.kakao;

import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchRequest;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;
import com.dnd.moyeolak.global.client.kakao.dto.KeywordSearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoLocalClient2 {

    private static final String KAKAO_LOCAL_BASE_URL = "https://dapi.kakao.com/v2/local/search";

    private final RestClient restClient;

    public KakaoLocalClient2(@Value("${kakao.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(KAKAO_LOCAL_BASE_URL)
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    public CategorySearchResponse searchByKeyword(KeywordSearchRequest request) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/keyword.json");
                    uriBuilder.queryParam("query", request.query());

                    if (request.x() != null && request.y() != null) {
                        uriBuilder.queryParam("x", request.x());
                        uriBuilder.queryParam("y", request.y());
                    }
                    if (request.radius() != null) {
                        uriBuilder.queryParam("radius", request.radius());
                    }
                    if (request.rect() != null) {
                        uriBuilder.queryParam("rect", request.rect());
                    }
                    if (request.page() != null) {
                        uriBuilder.queryParam("page", request.page());
                    }
                    if (request.size() != null) {
                        uriBuilder.queryParam("size", request.size());
                    }
                    if (request.sort() != null) {
                        uriBuilder.queryParam("sort", request.sort());
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(CategorySearchResponse.class);
    }

    public CategorySearchResponse searchByCategory(CategorySearchRequest request) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/category.json");
                    uriBuilder.queryParam("category_group_code", request.categoryGroupCode());

                    if (request.x() != null && request.y() != null) {
                        uriBuilder.queryParam("x", request.x());
                        uriBuilder.queryParam("y", request.y());
                    }
                    if (request.radius() != null) {
                        uriBuilder.queryParam("radius", request.radius());
                    }
                    if (request.rect() != null) {
                        uriBuilder.queryParam("rect", request.rect());
                    }
                    if (request.page() != null) {
                        uriBuilder.queryParam("page", request.page());
                    }
                    if (request.size() != null) {
                        uriBuilder.queryParam("size", request.size());
                    }
                    if (request.sort() != null) {
                        uriBuilder.queryParam("sort", request.sort());
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(CategorySearchResponse.class);
    }
}
