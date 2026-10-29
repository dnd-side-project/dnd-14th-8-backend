package com.dnd.moyeolak.global.config;

import com.dnd.moyeolak.global.metrics.MetricsSnapshotJob;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"prod", "render"})
@Testcontainers(disabledWithoutDocker = true)
class RenderDeploymentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName
            .parse("imresamu/postgis:15-3.5")
            .asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_JDBC_URL", postgres::getJdbcUrl);
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("KAKAO_API_KEY", () -> "test-kakao-key");
        registry.add("GOOGLE_API_KEY", () -> "test-google-key");
        registry.add("ODSAY_API_KEY", () -> "test-odsay-key");
    }

    @LocalServerPort
    private int port;

    @MockitoSpyBean
    private DataSource dataSource;

    @Autowired
    private ApplicationContext context;

    @Test
    void readinessChecksDoNotWakeTheDatabase() throws Exception {
        // 전체 health는 실제 DB를 확인한다. Render 전용 readiness는 상태만 확인해야 한다.
        assertThat(get("/actuator/health").statusCode()).isEqualTo(200);
        clearInvocations(dataSource);

        for (int i = 0; i < 3; i++) {
            HttpResponse<String> response = get("/actuator/health/readiness");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\":\"UP\"");
            assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
                    .contains("https://moyeorak.site");
        }

        verify(dataSource, never()).getConnection();
    }

    @Test
    void backgroundDatabaseJobsAreNotScheduled() {
        assertThat(context.getBeansOfType(MetricsSnapshotJob.class)).isEmpty();
        assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).isEmpty();
    }

    @Test
    void migratedDatabaseServesTheFrontend() throws Exception {
        HttpResponse<String> response = get("/api/meetings/stats");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("todayCreatedMeetingCount");
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                            .header("Origin", "https://moyeorak.site").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
        }
    }
}
