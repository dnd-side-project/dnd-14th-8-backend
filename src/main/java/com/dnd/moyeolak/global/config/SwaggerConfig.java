package com.dnd.moyeolak.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local 환경");
        Server devServer = new Server()
                .url("http://prod-moyeorak-alb-127485029-3acfa245edb4.kr.lb.naverncp.com")
                .description("Dev 환경");
        Server prodServer = new Server()
                .url("http://prod-moyeorak-alb-127485029-3acfa245edb4.kr.lb.naverncp.com")
                .description("Production 환경");

        return new OpenAPI()
                .servers(List.of(localServer, devServer, prodServer))
                .info(new Info()
                        .title("모여락 API")
                        .description("모여락 서비스 API 문서")
                        .version("v1.0.0"));
    }
}
