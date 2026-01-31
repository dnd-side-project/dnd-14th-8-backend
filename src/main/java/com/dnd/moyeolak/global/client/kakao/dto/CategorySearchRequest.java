package com.dnd.moyeolak.global.client.kakao.dto;

public record CategorySearchRequest(
        String categoryGroupCode,  // 필수: MT1(대형마트), CS2(편의점), PS3(주유소), SC4(학교) 등
        String x,                  // 중심 좌표 경도
        String y,                  // 중심 좌표 위도
        Integer radius,            // 반경 (0~20000m)
        String rect,               // 사각형 범위 (좌측X,좌측Y,우측X,우측Y)
        Integer page,              // 페이지 번호 (1~45)
        Integer size,              // 페이지당 결과 수 (1~15)
        String sort                // 정렬 기준 (distance, accuracy)
) {
    private static final Integer DEFAULT_PAGE = 1;
    private static final Integer DEFAULT_SIZE = 15;
    private static final String DEFAULT_SORT = "distance";

    public static CategorySearchRequest of(String categoryGroupCode, String x, String y, Integer radius) {
        return CategorySearchRequest.of(categoryGroupCode, x, y, radius, DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT);
    }

    public static CategorySearchRequest of(String categoryGroupCode, String x, String y, Integer radius, int page, int size, String sort) {
        return new CategorySearchRequest(categoryGroupCode, x, y, radius, null, page, size, sort);
    }
}
