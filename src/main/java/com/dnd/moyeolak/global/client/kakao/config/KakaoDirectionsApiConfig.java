package com.dnd.moyeolak.global.client.kakao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoDirectionsApiConfig {

    @Value("${kakao.api.key}")
    private String kakaoDirectionsApiKey;

    public String getApiKey() {
        return kakaoDirectionsApiKey;
    }

    @Bean("kakaoDirectionsRestTemplate")
    public RestTemplate kakaoDirectionsRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
