package com.dnd.moyeolak.global.client.google.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GoogleApiConfig {

    @Value("${google.api.key}")
    private String googleApiKey;

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    @Bean("googleRestTemplate")
    public RestTemplate googleRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
