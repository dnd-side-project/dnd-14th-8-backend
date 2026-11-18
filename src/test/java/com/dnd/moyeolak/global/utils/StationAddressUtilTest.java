package com.dnd.moyeolak.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StationAddressUtilTest {

    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("특별시와 광역시는 시도 이름을 줄이고 구까지 남긴다")
    @CsvSource(delimiter = '|', value = {
            "서울특별시 중구 을지로 지하106(을지로3가) | 서울 중구",
            "서울시 도봉구 도봉로 170길 2 | 서울 도봉구",
            "인천광역시 계양구 다남로 24 | 인천 계양구",
            "인천시 미추홀구 주안로 95-19 | 인천 미추홀구",
            "인천광역시 서해구 서곶로 294 | 인천 서해구",
    })
    void shortensMetropolitanAddress(String address, String expected) {
        assertThat(StationAddressUtil.shorten(address)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("구가 있는 시는 구까지 남긴다")
    @CsvSource(delimiter = '|', value = {
            "경기도 성남시 분당구 판교역로 지하 160 | 경기 성남시 분당구",
            "충남 천안시 서북구 두정로 289(두정동 84-1) | 충남 천안시 서북구",
            "충청남도 천안시 서북구 쌍용19로 20 | 충남 천안시 서북구",
            "경기도 화성시 병점구 떡전골로 97 | 경기 화성시 병점구",
    })
    void keepsDistrictOfCity(String address, String expected) {
        assertThat(StationAddressUtil.shorten(address)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @DisplayName("구가 없는 시와 군은 시, 군까지 남긴다")
    @CsvSource(delimiter = '|', value = {
            "경기도 구리시 갈매동 경춘북로 229(갈매동 502-18) | 경기 구리시",
            "경기도 양평군 양평읍 원덕흑천길 138 | 경기 양평군",
            "강원도 춘천시 남산면 강촌로 150(방곡리 409) | 강원 춘천시",
            "경기도 광주시 순암로 451 | 경기 광주시",
    })
    void stopsAtCityOrCountyWithoutDistrict(String address, String expected) {
        assertThat(StationAddressUtil.shorten(address)).isEqualTo(expected);
    }

    @Test
    @DisplayName("띄어쓰기가 겹치거나 줄바꿈 없는 공백(NBSP)이 섞여도 같은 결과를 낸다")
    void normalizesWhitespace() {
        assertThat(StationAddressUtil.shorten("  경기도  용인시 기흥구  용구대로 2560 ")).isEqualTo("경기 용인시 기흥구");
        assertThat(StationAddressUtil.shorten("인천광역시 영종구 백운로414번길 75")).isEqualTo("인천 영종구");
    }

    @ParameterizedTest
    @DisplayName("주소가 없으면 null을 돌려준다")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void returnsNullWithoutAddress(String address) {
        assertThat(StationAddressUtil.shorten(address)).isNull();
    }

    @ParameterizedTest
    @DisplayName("모르는 시도이거나 시도 다음이 시, 군, 구가 아니면 null을 돌려준다")
    @ValueSource(strings = {
            "달서구 달구벌대로 지하1476 (용산동)",
            "인창동 경춘로 지하 243",
            "부산광역시 동래구 중앙대로 지하 1223",
            "서울특별시",
            "경기도 중부대로 460",
    })
    void returnsNullForUnparsableAddress(String address) {
        assertThat(StationAddressUtil.shorten(address)).isNull();
    }
}
