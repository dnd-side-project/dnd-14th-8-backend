package com.dnd.moyeolak.global.client.google;

import com.dnd.moyeolak.domain.location.dto.TransitRouteResult;
import com.dnd.moyeolak.global.client.google.config.GoogleRoutesApiConfig;
import com.dnd.moyeolak.global.client.google.dto.LatLng;
import com.dnd.moyeolak.global.client.google.dto.RouteMatrixEntry;
import com.dnd.moyeolak.global.client.google.dto.RouteMatrixRequest;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class GoogleRoutesClient {

    private static final String MATRIX_URL =
            "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix";
    private static final String TRAVEL_MODE_TRANSIT = "TRANSIT";
    // Routes API TRANSIT 매트릭스는 요청당 최대 100 엘리먼트(origins x destinations)
    private static final int MAX_MATRIX_ELEMENTS = 100;
    private static final String MATRIX_FIELD_MASK =
            "originIndex,destinationIndex,duration,distanceMeters,condition";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RestTemplate restTemplate;
    private final GoogleRoutesApiConfig config;

    public GoogleRoutesClient(
            @Qualifier("googleRoutesRestTemplate") RestTemplate restTemplate,
            GoogleRoutesApiConfig config
    ) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public List<List<TransitRouteResult>> computeTransitMatrix(
            List<LatLng> origins,
            List<LatLng> destinations,
            LocalDateTime departureTime
    ) {
        TransitRouteResult[][] grid = new TransitRouteResult[origins.size()][destinations.size()];
        for (TransitRouteResult[] row : grid) {
            Arrays.fill(row, TransitRouteResult.unreachable());
        }

        int originsPerRequest = Math.max(1, MAX_MATRIX_ELEMENTS / destinations.size());
        for (int offset = 0; offset < origins.size(); offset += originsPerRequest) {
            List<LatLng> chunk = origins.subList(offset, Math.min(offset + originsPerRequest, origins.size()));
            for (RouteMatrixEntry entry : requestMatrix(chunk, destinations, departureTime)) {
                if (!entry.routeExists()) {
                    continue;
                }
                grid[offset + entry.originIndex()][entry.destinationIndex()] = new TransitRouteResult(
                        (int) Math.round(entry.durationSeconds() / 60.0),
                        entry.safeDistanceMeters(),
                        true
                );
            }
        }

        return Arrays.stream(grid).map(List::of).toList();
    }

    private RouteMatrixEntry[] requestMatrix(
            List<LatLng> origins,
            List<LatLng> destinations,
            LocalDateTime departureTime
    ) {
        RouteMatrixRequest body = RouteMatrixRequest.of(
                origins, destinations, TRAVEL_MODE_TRANSIT, toRfc3339(departureTime));

        try {
            RouteMatrixEntry[] entries = restTemplate.postForObject(
                    MATRIX_URL, new HttpEntity<>(body, headers(MATRIX_FIELD_MASK)), RouteMatrixEntry[].class);
            if (entries == null) {
                log.error("Google Routes 매트릭스 응답이 비어있습니다.");
                throw new BusinessException(ErrorCode.GOOGLE_API_ERROR);
            }
            return entries;
        } catch (RestClientException e) {
            log.error("Google Routes 매트릭스 호출 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new BusinessException(ErrorCode.GOOGLE_API_ERROR);
        }
    }

    private HttpHeaders headers(String fieldMask) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", config.getGoogleApiKey());
        headers.set("X-Goog-FieldMask", fieldMask);
        return headers;
    }

    private String toRfc3339(LocalDateTime departureTime) {
        if (departureTime == null) {
            return null;
        }
        return departureTime.atZone(KST).toInstant().toString();
    }
}
