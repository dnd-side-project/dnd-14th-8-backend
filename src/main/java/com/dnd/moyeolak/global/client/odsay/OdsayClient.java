package com.dnd.moyeolak.global.client.odsay;

import tools.jackson.databind.ObjectMapper;
import com.dnd.moyeolak.global.client.odsay.config.OdsayApiConfig;
import com.dnd.moyeolak.global.client.odsay.dto.OdsayPathInfo;
import com.dnd.moyeolak.global.client.odsay.dto.OdsayPathResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class OdsayClient {

    private final RestTemplate odsayRestTemplate;
    private final OdsayApiConfig odsayApiConfig;

    private static final String ODSAY_PATH_API_URL = "https://api.odsay.com/v1/api/searchPubTransPathT";

    public OdsayClient(
        @Qualifier("odsayRestTemplate") RestTemplate odsayRestTemplate,
        OdsayApiConfig odsayApiConfig
    ) {
        this.odsayRestTemplate = odsayRestTemplate;
        this.odsayApiConfig = odsayApiConfig;
    }

    public OdsayPathInfo searchRoute(
        double startLat,
        double startLng,
        double endLat,
        double endLng
    ) {
        String url = UriComponentsBuilder.fromUriString(ODSAY_PATH_API_URL)
            .queryParam("lang", 0)
            .queryParam("SX", startLng)
            .queryParam("SY", startLat)
            .queryParam("EX", endLng)
            .queryParam("EY", endLat)
            .queryParam("SearchType", 0)
            .queryParam("apiKey", odsayApiConfig.getOdsayApiKey())
            .build()
            .toUriString();

        log.debug("ODsay API 요청 URL: {}", url);

        try {
            // 먼저 raw JSON으로 받아서 구조 확인
            String rawResponse = odsayRestTemplate.getForObject(url, String.class);
//            log.info("ODsay API raw 응답: {}", rawResponse);

            ObjectMapper mapper = new ObjectMapper();
            OdsayPathResponse response = mapper.readValue(rawResponse, OdsayPathResponse.class);

            if (response == null || response.result() == null || response.result().path().isEmpty()) {
                log.warn("ODsay API 응답이 비어있습니다.");
                return new OdsayPathInfo(999, 0, 0, 0, 0, 0, 0);
            }

            return response.result().path().get(0).info();

        } catch (Exception e) {
            log.error("ODsay API 호출 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return new OdsayPathInfo(999, 0, 0, 0, 0, 0, 0);
        }
    }
}
