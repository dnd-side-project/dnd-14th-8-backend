package com.dnd.moyeolak.global.client.kakao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoApiConfig {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public String getKakaoApiKey() {
        return kakaoApiKey;
    }

    @Bean("kakaoRestTemplate")
    public RestTemplate kakaoRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
