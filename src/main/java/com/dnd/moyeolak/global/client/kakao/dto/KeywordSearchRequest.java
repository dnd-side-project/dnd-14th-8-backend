package com.dnd.moyeolak.global.client.kakao.dto;

public record KeywordSearchRequest(
        String query,              // 필수: 검색어
        String x,                  // 중심 좌표 경도
        String y,                  // 중심 좌표 위도
        Integer radius,            // 반경 (0~20000m)
        String rect,               // 사각형 범위 (좌측X,좌측Y,우측X,우측Y)
        Integer page,              // 페이지 번호 (1~45)
        Integer size,              // 페이지당 결과 수 (1~15)
        String sort                // 정렬 기준 (distance, accuracy)
) {
    public static KeywordSearchRequest of(String query) {
        return new KeywordSearchRequest(query, null, null, null, null, null, null, null);
    }

    public static KeywordSearchRequest of(String query, int page, int size) {
        return new KeywordSearchRequest(query, null, null, null, null, page, size, null);
    }

    public static KeywordSearchRequest of(String query, String x, String y, Integer radius) {
        return new KeywordSearchRequest(query, x, y, radius, null, null, null, "distance");
    }
}
