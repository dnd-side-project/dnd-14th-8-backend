package com.dnd.moyeolak.global.client.odsay.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdsayApiConfigTest {

    @Test
    @DisplayName("프록시 URL이 없으면 프록시 없이 직접 호출한다")
    void usesDirectConnectionWithoutProxyUrl() {
        assertThat(proxyOf(restTemplateWith(""))).isNull();
    }

    @Test
    @DisplayName("프록시 URL이 있으면 해당 호스트와 포트의 HTTP 프록시를 사용한다")
    void usesHttpProxyWithProxyUrl() {
        Proxy proxy = proxyOf(restTemplateWith("http://fixie:secret@proxy.example.com:8080"));

        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertThat(address.getHostString()).isEqualTo("proxy.example.com");
        assertThat(address.getPort()).isEqualTo(8080);
    }

    @Test
    @DisplayName("프록시 URL에 포트가 없으면 80을 사용한다")
    void defaultsProxyPortTo80() {
        Proxy proxy = proxyOf(restTemplateWith("http://fixie:secret@proxy.example.com"));

        assertThat(((InetSocketAddress) proxy.address()).getPort()).isEqualTo(80);
    }

    @Test
    @DisplayName("호스트가 없는 프록시 URL이면 기동 시 실패한다")
    void failsOnInvalidProxyUrl() {
        assertThatThrownBy(() -> restTemplateWith("not-a-url"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("프록시 인증 정보는 프록시 인증 요청에만 제공한다")
    void providesCredentialsOnlyToProxy() {
        Authenticator authenticator = OdsayApiConfig.proxyAuthenticator("fixie:se:cret");

        PasswordAuthentication proxyAuth = request(authenticator, Authenticator.RequestorType.PROXY);
        assertThat(proxyAuth.getUserName()).isEqualTo("fixie");
        assertThat(proxyAuth.getPassword()).isEqualTo("se:cret".toCharArray());
        assertThat(request(authenticator, Authenticator.RequestorType.SERVER)).isNull();
    }

    @Test
    @DisplayName("프록시 URL에 인증 정보가 없으면 인증하지 않는다")
    void skipsAuthenticatorWithoutCredentials() {
        assertThat(OdsayApiConfig.proxyAuthenticator(null)).isNull();
        assertThat(OdsayApiConfig.proxyAuthenticator("fixie")).isNull();
    }

    private RestTemplate restTemplateWith(String proxyUrl) {
        OdsayApiConfig config = new OdsayApiConfig();
        ReflectionTestUtils.setField(config, "odsayProxyUrl", proxyUrl);
        return config.odsayRestTemplate();
    }

    private Proxy proxyOf(RestTemplate restTemplate) {
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        return (Proxy) ReflectionTestUtils.getField(factory, "proxy");
    }

    private PasswordAuthentication request(Authenticator authenticator, Authenticator.RequestorType type) {
        return authenticator.requestPasswordAuthenticationInstance(
                "proxy.example.com", null, 80, "http", "", "basic", null, type);
    }
}
