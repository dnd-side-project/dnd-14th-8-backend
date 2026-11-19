package com.dnd.moyeolak.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StationNameUtilTest {

    @ParameterizedTest(name = "{0}")
    @DisplayName("노선마다 이름이 다른 총신대입구(4호선)와 이수(7호선)는 합친 이름 하나로 본다")
    @ValueSource(strings = {"총신대입구", "이수"})
    void sharesNameOfTransferStationWithDifferentNames(String name) {
        assertThat(StationNameUtil.sharedName(name)).isEqualTo("총신대입구(이수)");
        assertThat(StationNameUtil.lineNamesOf(name)).containsExactlyInAnyOrder("총신대입구", "이수");
    }

    @Test
    @DisplayName("예외 표에 없는 역은 받은 이름 그대로다")
    void keepsOtherStationNames() {
        assertThat(StationNameUtil.sharedName("을지로3가")).isEqualTo("을지로3가");
        assertThat(StationNameUtil.lineNamesOf("을지로3가")).containsExactly("을지로3가");
    }
}
