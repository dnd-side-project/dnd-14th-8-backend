package com.dnd.moyeolak.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dnd.moyeolak.global.station.entity.Station;
import com.dnd.moyeolak.global.station.repository.StationRepository;
import com.dnd.moyeolak.global.utils.StationAddressUtil;
import com.dnd.moyeolak.global.utils.StationLineUtil;
import com.dnd.moyeolak.global.utils.StationNameUtil;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PostgisFlywayMigrationTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:15-3.3")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGIS_IMAGE)
            .withDatabaseName("moyeolak")
            .withUsername("moyeolak")
            .withPassword("moyeolak");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private StationRepository stationRepository;

    @Test
    void shouldApplyFlywayMigrationsAndValidatePostgisSchema() {
        Integer appliedMigrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class
        );
        Integer stationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stations", Integer.class);
        String postgisVersion = jdbcTemplate.queryForObject("SELECT PostGIS_Version()", String.class);

        assertThat(appliedMigrationCount).isEqualTo(9);
        assertThat(stationCount).isPositive();
        assertThat(postgisVersion).isNotBlank();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("9");
    }

    @Test
    void shouldAddNullableDepartureAddressToLocationVote() {
        Map<String, Object> column = jdbcTemplate.queryForMap(
                "SELECT data_type, character_maximum_length, is_nullable FROM information_schema.columns "
                        + "WHERE table_name = 'location_vote' AND column_name = 'departure_address'"
        );

        assertThat(column.get("data_type")).isEqualTo("character varying");
        assertThat(((Number) column.get("character_maximum_length")).intValue()).isEqualTo(255);
        assertThat(column.get("is_nullable")).isEqualTo("YES");
    }

    @Test
    void shouldAddNullableAddressToStations() {
        Map<String, Object> column = jdbcTemplate.queryForMap(
                "SELECT data_type, character_maximum_length, is_nullable FROM information_schema.columns "
                        + "WHERE table_name = 'stations' AND column_name = 'address'"
        );

        assertThat(column.get("data_type")).isEqualTo("character varying");
        assertThat(((Number) column.get("character_maximum_length")).intValue()).isEqualTo(255);
        assertThat(column.get("is_nullable")).isEqualTo("YES");
    }

    @Test
    void shouldFillEveryStationAddress() {
        Integer stationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stations", Integer.class);
        List<String> withoutAddress = jdbcTemplate.queryForList(
                "SELECT name || ' ' || line FROM stations WHERE address IS NULL", String.class);
        String euljiro3ga = jdbcTemplate.queryForObject(
                "SELECT address FROM stations WHERE name = '을지로3가' AND line = '2호선'", String.class);

        // V2의 654행에서 V8이 노선이 틀린 4행을 지웠고, V9가 빠져 있던 149행을 넣었다.
        assertThat(stationCount).isEqualTo(799);
        // V7에서 비어 있던 행주는 V8에서 한국항공대로 고치며, 킨텍스는 V9에서 주소를 넣었다.
        assertThat(withoutAddress).isEmpty();
        assertThat(euljiro3ga).isEqualTo("서울특별시 중구 을지로 지하106(을지로3가)");
    }

    @Test
    void shouldUseCurrentDistrictNamesInStationAddresses() {
        // 출처 데이터에 옛 행정구역 이름으로 남아 있던 곳이다. 인천은 2026-07-01 개편, 화성시는 2026-02-01 구 설치.
        Integer oldIncheonDistricts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stations WHERE address ~ '^인천(광역시|시) (남구|중구|동구|서구) '", Integer.class);
        Integer hwaseongWithoutDistrict = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stations WHERE address ~ '^경기도 화성시' AND address !~ '^경기도 화성시 \\S+구( |$)'",
                Integer.class);
        List<String> sample = jdbcTemplate.queryForList(
                "SELECT address FROM stations WHERE (name, line) IN (('검단오류', '인천2호선'), ('인천공항1터미널', '공항철도'), "
                        + "('동인천', '1호선'), ('서해구청', '인천2호선'), ('병점', '1호선'), ('주안', '1호선'))", String.class);

        assertThat(oldIncheonDistricts).isZero();
        assertThat(hwaseongWithoutDistrict).isZero();
        assertThat(sample).extracting(StationAddressUtil::shorten).containsExactlyInAnyOrder(
                "인천 검단구", "인천 영종구", "인천 제물포구", "인천 서해구", "경기 화성시 병점구", "인천 미추홀구");
    }

    @Test
    void shouldRemoveStationsWithWrongLineAndUseCurrentNames() {
        List<String> removed = jdbcTemplate.queryForList(
                "SELECT name || ' ' || line FROM stations WHERE (name, line) IN (('김포공항', '경의중앙선'), "
                        + "('신길', '경의중앙선'), ('가산디지털단지', '경의중앙선'), ('삼성', 'GTX-A'))", String.class);
        List<String> oldNames = jdbcTemplate.queryForList(
                "SELECT name FROM stations WHERE name IN ('지제', '당고개', '뚝섬유원지', '서구청', '행주')", String.class);
        List<String> currentNames = jdbcTemplate.queryForList(
                "SELECT name || ' ' || line FROM stations "
                        + "WHERE name IN ('평택지제', '불암산', '자양', '서해구청', '한국항공대', '신길온천')", String.class);
        // 같은 역의 맞는 노선 행은 남아 있다. 김포골드라인과 서해선은 V9에서 넣었다.
        List<String> gimpoAirportLines = jdbcTemplate.queryForList(
                "SELECT line FROM stations WHERE name = '김포공항'", String.class);

        assertThat(removed).isEmpty();
        assertThat(oldNames).isEmpty();
        assertThat(currentNames).containsExactlyInAnyOrder(
                "평택지제 1호선", "불암산 4호선", "자양 7호선", "서해구청 인천2호선", "한국항공대 경의중앙선",
                "신길온천 4호선", "신길온천 수인분당선");
        assertThat(gimpoAirportLines).containsExactlyInAnyOrder("5호선", "9호선", "공항철도", "김포골드라인", "서해선");
    }

    @Test
    void shouldFixStationCoordinates() {
        // 송탄역은 V2 시드에서 10km쯤 떨어져 있었다. 위키 좌표(37.0757, 127.0543)에서 100m 안이어야 한다.
        Double songtanMeters = distanceMeters("송탄", "1호선", 37.0757, 127.0543);
        // 공공데이터의 양원 좌표는 경북 쪽으로 틀려 있어 위키데이터 좌표를 넣었다.
        Double yangwonMeters = distanceMeters("양원", "경의중앙선", 37.606539, 127.107889);
        // 2호선은 을지로3가, 을지로4가, 동대문역사문화공원 순서로 동쪽에 있다. V2 시드는 을지로4가가 가장 동쪽이었다.
        List<String> westToEast = jdbcTemplate.queryForList(
                "SELECT name FROM stations WHERE line = '2호선' "
                        + "AND name IN ('을지로3가', '을지로4가', '동대문역사문화공원') ORDER BY ST_X(geom)", String.class);
        // 서울, 경기(연천까지), 인천, 강원(춘천), 충남(천안, 아산) 범위를 벗어난 좌표가 없다.
        Integer outOfArea = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stations WHERE ST_Y(geom) NOT BETWEEN 36.7 AND 38.2 "
                        + "OR ST_X(geom) NOT BETWEEN 126.3 AND 127.8", Integer.class);

        assertThat(songtanMeters).isLessThan(100.0);
        assertThat(yangwonMeters).isLessThan(1.0);
        assertThat(westToEast).containsExactly("을지로3가", "을지로4가", "동대문역사문화공원");
        assertThat(outOfArea).isZero();
    }

    @Test
    void shouldAddStationsMissingFromSeed() {
        // V9가 넣은 노선. 시드(V2)에는 이 노선들이 통째로 없었다.
        Map<String, Long> newLineCounts = new java.util.TreeMap<>();
        jdbcTemplate.query(
                "SELECT line, COUNT(*) FROM stations WHERE line IN ('서해선', '에버라인', '의정부경전철', '우이신설선', "
                        + "'신림선', '김포골드라인') GROUP BY line",
                rs -> { newLineCounts.put(rs.getString(1), rs.getLong(2)); });
        List<String> gtxA = jdbcTemplate.queryForList(
                "SELECT name FROM stations WHERE line = 'GTX-A'", String.class);
        // 있던 노선에서 빠져 있던 역의 표본: 2호선 지선, 1호선 연천 연장, 8호선 별내 연장
        List<String> sample = jdbcTemplate.queryForList(
                "SELECT name || ' ' || line FROM stations WHERE (name, line) IN (('까치산', '2호선'), ('신설동', '2호선'), "
                        + "('연천', '1호선'), ('별내', '8호선'), ('시청.용인대', '에버라인'))", String.class);
        // 같은 역이 같은 노선으로 두 번 들어가지 않았다.
        Integer duplicated = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (SELECT name, line FROM stations GROUP BY name, line HAVING COUNT(*) > 1) t",
                Integer.class);
        // 운정중앙은 공공데이터에 없어 위키데이터 좌표(Q125895818)를 넣었다.
        Double unjeongJungangMeters = distanceMeters("운정중앙", "GTX-A", 37.716610, 126.728354);

        assertThat(newLineCounts).containsExactlyInAnyOrderEntriesOf(Map.of(
                "서해선", 21L, "에버라인", 15L, "의정부경전철", 15L, "우이신설선", 13L, "신림선", 11L, "김포골드라인", 10L));
        assertThat(gtxA).containsExactlyInAnyOrder(
                "운정중앙", "킨텍스", "대곡", "연신내", "서울역", "수서", "성남", "구성", "동탄");
        assertThat(sample).hasSize(5);
        assertThat(duplicated).isZero();
        assertThat(unjeongJungangMeters).isLessThan(1.0);
    }

    @Test
    void shouldKeepSameNamedStationsClearlyTogetherOrApart() {
        // 추천에서 "이름이 같고 500m 안이면 같은 역"으로 묶는다(MidpointRecommendationServiceImpl).
        // 같은 이름의 행 사이 거리가 이 기준 근처(400m와 600m 사이)에 있으면 역을 추가하거나 좌표를 고칠 때 기준을 다시 봐야 한다.
        List<String> nearThreshold = jdbcTemplate.queryForList(
                "SELECT a.name || ' ' || a.line || ' / ' || b.line || ' ' || round(ST_Distance(a.geom::geography, b.geom::geography)) "
                        + "FROM stations a JOIN stations b ON a.name = b.name AND a.id < b.id "
                        + "WHERE ST_Distance(a.geom::geography, b.geom::geography) BETWEEN 400 AND 600", String.class);
        Double seoulStationSpread = jdbcTemplate.queryForObject(
                "SELECT max(ST_Distance(a.geom::geography, b.geom::geography)) FROM stations a JOIN stations b "
                        + "ON a.name = b.name AND a.id < b.id WHERE a.name = '서울역'", Double.class);
        Double sinchonDistance = jdbcTemplate.queryForObject(
                "SELECT ST_Distance(a.geom::geography, b.geom::geography) FROM stations a JOIN stations b "
                        + "ON a.name = b.name AND a.id < b.id WHERE a.name = '신촌'", Double.class);

        // 서울역은 같은 역의 노선별 승강장이 가장 멀리 떨어진 경우다(1호선과 공항철도).
        assertThat(nearThreshold).allMatch(pair -> pair.startsWith("서울역 "));
        assertThat(seoulStationSpread).isLessThan(500.0);
        // 신촌(2호선)과 신촌(경의중앙선)은 이름만 같은 다른 역이다.
        assertThat(sinchonDistance).isGreaterThan(500.0);
    }

    @Test
    void shouldFindEveryLineRowOfStationByName() {
        // 추천역의 노선(lines)은 이름으로 찾은 행에서 모은다(#190). 후보 10행에 없는 노선 행도 여기서 나와야 한다.
        Map<String, List<String>> linesByName = stationRepository.findByNameIn(List.of("서울역", "김포공항", "신촌", "양평")).stream()
                .collect(Collectors.groupingBy(Station::getName,
                        Collectors.collectingAndThen(Collectors.mapping(Station::getLine, Collectors.toList()),
                                StationLineUtil::sort)));

        assertThat(linesByName.get("서울역")).containsExactly("1호선", "4호선", "경의중앙선", "공항철도", "GTX-A");
        assertThat(linesByName.get("김포공항")).containsExactly("5호선", "9호선", "공항철도", "김포골드라인", "서해선");
        // 신촌과 양평은 이름만 같은 다른 역 두 곳이 함께 나온다. 같은 역인지는 추천 쪽에서 거리(500m)로 거른다.
        assertThat(linesByName.get("신촌")).containsExactly("2호선", "경의중앙선");
        assertThat(linesByName.get("양평")).containsExactly("5호선", "경의중앙선");
        Double yangpyeongDistance = jdbcTemplate.queryForObject(
                "SELECT ST_Distance(a.geom::geography, b.geom::geography) FROM stations a JOIN stations b "
                        + "ON a.name = b.name AND a.id < b.id WHERE a.name = '양평'", Double.class);
        assertThat(yangpyeongDistance).isGreaterThan(500.0);
    }

    @Test
    void shouldReviewNearbyStationsWithDifferentNames() {
        // 노선마다 이름이 다른 같은 환승역은 StationNameUtil의 예외 표로 묶는다(#190).
        // 이름이 다르고 300m 안인 행 쌍이 새로 생기면 같은 역인지 확인하고, 같은 역이면 예외 표에 넣는다.
        List<String> nearbyPairs = jdbcTemplate.queryForList(
                "SELECT least(a.name COLLATE \"C\", b.name COLLATE \"C\") || ' / ' || greatest(a.name COLLATE \"C\", b.name COLLATE \"C\") "
                        + "FROM stations a JOIN stations b ON a.name <> b.name AND a.id < b.id "
                        + "WHERE ST_Distance(a.geom::geography, b.geom::geography) <= 300 "
                        + "GROUP BY 1 ORDER BY 1", String.class);

        // 근거는 레일포털 "전체_도시철도역사정보"(2026-06 기준)의 환승역구분과 환승노선명이다.
        // 총신대입구(4호선)와 이수(7호선)는 서로를 환승 노선으로 적은 같은 환승역이다(예외 표에 있음).
        // 의정부(1호선)와 경전철의정부, 신용산(4호선)은 일반역이고, 용산의 환승(경의중앙선)은 용산 자기 행에서 이뤄진다. 둘 다 다른 역이다.
        assertThat(nearbyPairs).containsExactlyInAnyOrder("경전철의정부 / 의정부", "신용산 / 용산", "이수 / 총신대입구");
        assertThat(StationNameUtil.sharedName("총신대입구")).isEqualTo(StationNameUtil.sharedName("이수"));
        assertThat(StationNameUtil.sharedName("의정부")).isNotEqualTo(StationNameUtil.sharedName("경전철의정부"));
        assertThat(StationNameUtil.sharedName("용산")).isNotEqualTo(StationNameUtil.sharedName("신용산"));
        // 같은 역으로 묶으려면 두 행이 500m 안이어야 한다.
        Double chongshinIsuDistance = jdbcTemplate.queryForObject(
                "SELECT ST_Distance(a.geom::geography, b.geom::geography) FROM stations a, stations b "
                        + "WHERE a.name = '총신대입구' AND b.name = '이수'", Double.class);
        assertThat(chongshinIsuDistance).isLessThan(500.0);
    }

    private Double distanceMeters(String name, String line, double latitude, double longitude) {
        return jdbcTemplate.queryForObject(
                "SELECT ST_Distance(geom::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) "
                        + "FROM stations WHERE name = ? AND line = ?",
                Double.class, longitude, latitude, name, line);
    }

    @Test
    void shouldShortenEveryStoredStationAddress() {
        List<String> addresses = jdbcTemplate.queryForList(
                "SELECT address FROM stations WHERE address IS NOT NULL", String.class);

        // 저장한 주소는 모두 "시도 줄임 + 구까지"로 줄일 수 있어야 한다. 줄이지 못하면 화면에 주소 줄이 빠진다.
        assertThat(addresses)
                .hasSize(799)
                .allSatisfy(address -> assertThat(StationAddressUtil.shorten(address))
                        .as(address)
                        .matches("(서울|경기|인천|강원|충남) \\S+[시군구]( \\S+구)?"));
    }
}
