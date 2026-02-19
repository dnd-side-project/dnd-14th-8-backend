package com.dnd.moyeolak.global.client.kakao.dto;

public record KeywordSearchRequest(
        String query,              // 필수: 검색어
        String x,                  // 중심 좌표 경도
        String y,                  // 중심 좌표 위도
        Integer radius,            // 반경 (0~20000m)
        String sort                // 정렬 기준 (distance, accuracy)
) {
    private final static int DEFAULT_RADIUS = 5;

    public static KeywordSearchRequest of(String query, String x, String y) {
        return new KeywordSearchRequest(query, x, y, DEFAULT_RADIUS,  "distance");
    }
}
