package com.dnd.moyeolak.global.client.odsay.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;

@Slf4j
@Configuration
public class OdsayApiConfig {

    @Value("${odsay.api.key}")
    private String odsayApiKey;

    // ODsay Server 키는 개별 IP만 등록할 수 있어, 고정 IP가 없는 운영(Render)에서는
    // 고정 IP 프록시(Fixie)를 거쳐 호출한다. 비어 있으면 프록시 없이 직접 호출한다.
    @Value("${odsay.api.proxy-url:}")
    private String odsayProxyUrl;

    public String getOdsayApiKey() {
        return odsayApiKey;
    }

    @Bean("odsayRestTemplate")
    public RestTemplate odsayRestTemplate() {
        SimpleClientHttpRequestFactory factory = StringUtils.hasText(odsayProxyUrl)
                ? proxiedRequestFactory(URI.create(odsayProxyUrl.trim()))
                : new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    private SimpleClientHttpRequestFactory proxiedRequestFactory(URI proxyUri) {
        if (proxyUri.getHost() == null) {
            throw new IllegalStateException("odsay.api.proxy-url 형식이 올바르지 않습니다. 예: http://user:password@host:80");
        }
        int port = proxyUri.getPort() == -1 ? 80 : proxyUri.getPort();
        Authenticator authenticator = proxyAuthenticator(proxyUri.getUserInfo());

        // 인증 정보는 JVM 전역이 아니라 ODsay 연결에만 붙인다.
        // HTTPS 터널(CONNECT)의 Basic 인증은 JVM 옵션 jdk.http.auth.tunneling.disabledSchemes를 비워야 동작한다(Dockerfile).
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                if (authenticator != null) {
                    connection.setAuthenticator(authenticator);
                }
            }
        };
        factory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUri.getHost(), port)));
        log.info("ODsay 호출에 프록시를 사용합니다: {}:{}", proxyUri.getHost(), port);
        return factory;
    }

    static Authenticator proxyAuthenticator(String userInfo) {
        if (!StringUtils.hasText(userInfo) || !userInfo.contains(":")) {
            return null;
        }
        String[] credentials = userInfo.split(":", 2);
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() != RequestorType.PROXY) {
                    return null;
                }
                return new PasswordAuthentication(credentials[0], credentials[1].toCharArray());
            }
        };
    }
}
