package com.dnd.moyeolak.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StationLineUtilTest {

    @Test
    @DisplayName("번호 노선을 번호순으로 먼저 두고, 그다음 가나다순, 영문 노선은 마지막에 둔다")
    void sortsSeoulStationLines() {
        List<String> sorted = StationLineUtil.sort(List.of("GTX-A", "공항철도", "4호선", "경의중앙선", "1호선"));

        assertThat(sorted).containsExactly("1호선", "4호선", "경의중앙선", "공항철도", "GTX-A");
    }

    @Test
    @DisplayName("이름 노선끼리는 가나다순이다")
    void sortsNamedLinesInKoreanOrder() {
        List<String> sorted = StationLineUtil.sort(List.of("서해선", "김포골드라인", "9호선", "공항철도", "5호선"));

        assertThat(sorted).containsExactly("5호선", "9호선", "공항철도", "김포골드라인", "서해선");
    }

    @Test
    @DisplayName("인천1호선처럼 앞에 다른 글자가 붙은 노선은 번호 노선이 아니라 가나다순에 든다")
    void treatsPrefixedNumberLineAsNamedLine() {
        List<String> sorted = StationLineUtil.sort(List.of("인천2호선", "수인분당선", "인천1호선", "1호선"));

        assertThat(sorted).containsExactly("1호선", "수인분당선", "인천1호선", "인천2호선");
    }

    @Test
    @DisplayName("빈 값은 빼고 같은 노선은 한 번만 남긴다")
    void dropsBlankAndDuplicateLines() {
        List<String> sorted = StationLineUtil.sort(Arrays.asList("2호선", null, "", " ", "2호선", "7호선"));

        assertThat(sorted).containsExactly("2호선", "7호선");
    }

    @Test
    @DisplayName("노선이 하나면 그대로 돌려준다")
    void keepsSingleLine() {
        assertThat(StationLineUtil.sort(List.of("3호선"))).containsExactly("3호선");
    }
}
