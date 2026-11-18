package com.dnd.moyeolak.global.utils;

import java.util.Map;

public final class StationAddressUtil {

    private StationAddressUtil() {}

    /**
     * 시도 이름을 줄인 표. 역 주소 원본(stations.address)에 나오는 표기만 넣었다.
     * 원본에는 같은 시도가 "서울특별시", "서울시"처럼 섞여 있다.
     */
    private static final Map<String, String> SIDO_SHORT_NAMES = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("서울시", "서울"),
            Map.entry("서울", "서울"),
            Map.entry("경기도", "경기"),
            Map.entry("경기", "경기"),
            Map.entry("인천광역시", "인천"),
            Map.entry("인천시", "인천"),
            Map.entry("인천", "인천"),
            Map.entry("강원특별자치도", "강원"),
            Map.entry("강원도", "강원"),
            Map.entry("강원", "강원"),
            Map.entry("충청남도", "충남"),
            Map.entry("충남", "충남")
    );

    /**
     * 역 주소를 화면에 보여 줄 만큼만 줄인다. 시도 이름을 줄이고 구까지만 남긴다.
     *
     * <ul>
     *   <li>"서울특별시 중구 을지로 지하106(을지로3가)" → "서울 중구"</li>
     *   <li>"경기도 성남시 분당구 판교역로 지하 160" → "경기 성남시 분당구"</li>
     *   <li>"경기도 구리시 갈매동 경춘북로 229" → "경기 구리시" (구가 없는 시와 군은 시, 군까지)</li>
     * </ul>
     *
     * 주소가 없거나, 모르는 시도이거나, 시도 다음이 시, 군, 구가 아니면 null을 돌려준다.
     * 잘못 자른 글자를 보여 주는 것보다 주소 줄을 비우는 편이 낫다.
     */
    public static String shorten(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String[] tokens = address.replace(' ', ' ').strip().split("\\s+");
        if (tokens.length < 2) {
            return null;
        }

        String sido = SIDO_SHORT_NAMES.get(tokens[0]);
        String district = tokens[1];
        if (sido == null || !isDistrict(district)) {
            return null;
        }

        // 구가 있는 시("성남시 분당구")는 구까지 붙인다.
        if (district.endsWith("시") && tokens.length > 2 && tokens[2].endsWith("구")) {
            return sido + " " + district + " " + tokens[2];
        }
        return sido + " " + district;
    }

    private static boolean isDistrict(String token) {
        return token.endsWith("시") || token.endsWith("군") || token.endsWith("구");
    }
}
