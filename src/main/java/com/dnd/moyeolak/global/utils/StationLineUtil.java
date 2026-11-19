package com.dnd.moyeolak.global.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StationLineUtil {

    private StationLineUtil() {}

    private static final Pattern NUMBERED_LINE = Pattern.compile("([1-9])호선");

    private static final int NUMBERED = 0;
    private static final int NAMED = 1;
    private static final int LATIN = 2;

    private static final Comparator<String> DISPLAY_ORDER = Comparator
            .comparingInt(StationLineUtil::group)
            .thenComparingInt(StationLineUtil::number)
            .thenComparing(Comparator.naturalOrder());

    /**
     * 한 역의 노선을 화면에 보여 줄 순서로 정렬한다(#190).
     *
     * <ol>
     *   <li>"1호선"부터 "9호선"까지 번호순</li>
     *   <li>나머지는 이름의 가나다순("경의중앙선", "공항철도", "김포골드라인"). "인천1호선"처럼 앞에 다른 글자가 붙은 노선도 여기에 든다</li>
     *   <li>영문으로 시작하는 노선("GTX-A")은 마지막</li>
     * </ol>
     *
     * 빈 값은 빼고, 같은 노선은 한 번만 남긴다. 노선 이름은 stations.line 글자 그대로 둔다.
     */
    public static List<String> sort(Collection<String> lines) {
        return lines.stream()
                .filter(Objects::nonNull)
                .filter(line -> !line.isBlank())
                .distinct()
                .sorted(DISPLAY_ORDER)
                .toList();
    }

    private static int group(String line) {
        if (NUMBERED_LINE.matcher(line).matches()) {
            return NUMBERED;
        }
        char first = line.charAt(0);
        if ((first >= 'A' && first <= 'Z') || (first >= 'a' && first <= 'z')) {
            return LATIN;
        }
        return NAMED;
    }

    private static int number(String line) {
        Matcher matcher = NUMBERED_LINE.matcher(line);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
