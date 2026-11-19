package com.dnd.moyeolak.global.utils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StationNameUtil {

    private StationNameUtil() {}

    /**
     * 노선마다 이름이 다른 같은 환승역과, 그 역을 화면에 보여 줄 이름(#190).
     * stations에는 노선마다 카카오 이름으로 한 행씩 들어 있어(4호선 "총신대입구", 7호선 "이수") 이름만으로는 같은 역으로 묶이지 않는다.
     * 화면 이름은 어느 노선 행이 추천에 남든 같도록 두 이름을 합쳤다. 카카오에서 이 이름으로 검색해도 두 역이 모두 나온다.
     * 새 역이 들어오면 PostgisFlywayMigrationTest가 이름이 다른 가까운 행 쌍을 다시 보게 한다.
     */
    private static final Map<String, String> SHARED_NAME_BY_LINE_NAME = Map.of(
            "총신대입구", "총신대입구(이수)",
            "이수", "총신대입구(이수)"
    );

    /**
     * 같은 역인지 따지고 화면에 보여 줄 때 쓰는 이름. 예외 표에 있으면 합친 이름, 없으면 받은 이름 그대로다.
     */
    public static String sharedName(String name) {
        return SHARED_NAME_BY_LINE_NAME.getOrDefault(name, name);
    }

    /**
     * 같은 역의 stations 이름을 모두 돌려준다. 예외 표에 없으면 받은 이름 하나다.
     */
    public static Set<String> lineNamesOf(String name) {
        String shared = sharedName(name);
        return Stream.concat(
                        Stream.of(name),
                        SHARED_NAME_BY_LINE_NAME.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(shared))
                                .map(Map.Entry::getKey))
                .collect(Collectors.toUnmodifiableSet());
    }
}
