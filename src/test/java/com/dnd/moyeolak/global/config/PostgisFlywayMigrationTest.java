package com.dnd.moyeolak.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.dnd.moyeolak.global.utils.StationAddressUtil;
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

    @Test
    void shouldApplyFlywayMigrationsAndValidatePostgisSchema() {
        Integer appliedMigrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class
        );
        Integer stationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stations", Integer.class);
        String postgisVersion = jdbcTemplate.queryForObject("SELECT PostGIS_Version()", String.class);

        assertThat(appliedMigrationCount).isEqualTo(7);
        assertThat(stationCount).isPositive();
        assertThat(postgisVersion).isNotBlank();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("7");
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
    void shouldFillStationAddressesExceptStationsMissingFromSource() {
        Integer stationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stations", Integer.class);
        List<String> withoutAddress = jdbcTemplate.queryForList(
                "SELECT name || ' ' || line FROM stations WHERE address IS NULL", String.class);
        String euljiro3ga = jdbcTemplate.queryForObject(
                "SELECT address FROM stations WHERE name = '을지로3가' AND line = '2호선'", String.class);

        assertThat(stationCount).isEqualTo(654);
        // 출처 데이터에 없는 두 역만 주소가 비어 있다.
        assertThat(withoutAddress).containsExactlyInAnyOrder("킨텍스 GTX-A", "행주 경의중앙선");
        assertThat(euljiro3ga).isEqualTo("서울특별시 중구 을지로 지하106(을지로3가)");
    }

    @Test
    void shouldUseCurrentDistrictNamesInStationAddresses() {
        // 출처 데이터에 옛 행정구역 이름으로 남아 있던 곳이다. 인천은 2026-07-01 개편, 화성시는 2026-02-01 구 설치.
        Integer oldIncheonDistricts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stations WHERE address ~ '^인천(광역시|시) (남구|중구|동구|서구) '", Integer.class);
        Integer hwaseongWithoutDistrict = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stations WHERE address ~ '^경기도 화성시 ' AND address !~ '^경기도 화성시 \\S+구 '",
                Integer.class);
        List<String> sample = jdbcTemplate.queryForList(
                "SELECT address FROM stations WHERE (name, line) IN (('검단오류', '인천2호선'), ('인천공항1터미널', '공항철도'), "
                        + "('동인천', '1호선'), ('서구청', '인천2호선'), ('병점', '1호선'), ('주안', '1호선'))", String.class);

        assertThat(oldIncheonDistricts).isZero();
        assertThat(hwaseongWithoutDistrict).isZero();
        assertThat(sample).extracting(StationAddressUtil::shorten).containsExactlyInAnyOrder(
                "인천 검단구", "인천 영종구", "인천 제물포구", "인천 서해구", "경기 화성시 병점구", "인천 미추홀구");
    }

    @Test
    void shouldShortenEveryStoredStationAddress() {
        List<String> addresses = jdbcTemplate.queryForList(
                "SELECT address FROM stations WHERE address IS NOT NULL", String.class);

        // 저장한 주소는 모두 "시도 줄임 + 구까지"로 줄일 수 있어야 한다. 줄이지 못하면 화면에 주소 줄이 빠진다.
        assertThat(addresses)
                .hasSize(652)
                .allSatisfy(address -> assertThat(StationAddressUtil.shorten(address))
                        .as(address)
                        .matches("(서울|경기|인천|강원|충남) \\S+[시군구]( \\S+구)?"));
    }
}
