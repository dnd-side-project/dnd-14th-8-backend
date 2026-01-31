# MeetSpot 최적 만남 장소 추천 알고리즘 구현 가이드

## 개요
참가자들의 거주지를 기반으로 모두에게 최적의 만남 장소(지하철역)를 추천하는 시스템

---

## 프로젝트 구조

```
src/main/java/com/dnd/moyeolak/
├── global/
│   └── client/
│       ├── kakao/
│       │   ├── KakaoLocalClient.java
│       │   ├── dto/
│       │   │   ├── KakaoLocalResponse.java
│       │   │   └── KakaoDocument.java
│       │   └── config/
│       │       └── KakaoApiConfig.java
│       │
│       └── odsay/
│           ├── OdsayClient.java
│           ├── dto/
│           │   ├── OdsayPathResponse.java
│           │   ├── OdsayPathInfo.java
│           │   └── OdsaySubPath.java
│           └── config/
│               └── OdsayApiConfig.java
│
└── test/janghh/
    ├── controller/
    │   └── OptimalLocationController.java
    ├── service/
    │   ├── OptimalLocationService.java
    │   └── GeographicCalculator.java
    ├── dto/
    │   ├── request/
    │   │   └── OptimalLocationRequest.java
    │   └── response/
    │       ├── OptimalLocationResponse.java
    │       ├── EvaluatedPlace.java
    │       ├── RouteDetail.java
    │       ├── SubwayStation.java
    │       └── CenterPoint.java
    └── doc/
        ├── OMP_Algorithm_Implementation_Guide.md
        └── OMP_System_Architecture.md
```

---

## Step 1: 중간지점 계산

### GeographicCalculator.java

```java
package com.dnd.moyeolak.test.janghh.service;

import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest.ParticipantInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeographicCalculator {

    public double[] calculateGeographicCenter(List<ParticipantInfo> participants) {
        double x = 0, y = 0, z = 0;

        for (ParticipantInfo p : participants) {
            double latRad = Math.toRadians(p.latitude());
            double lngRad = Math.toRadians(p.longitude());

            x += Math.cos(latRad) * Math.cos(lngRad);
            y += Math.cos(latRad) * Math.sin(lngRad);
            z += Math.sin(latRad);
        }

        int n = participants.size();
        x /= n;
        y /= n;
        z /= n;

        double centerLng = Math.atan2(y, x);
        double hyp = Math.sqrt(x * x + y * y);
        double centerLat = Math.atan2(z, hyp);

        return new double[]{
            Math.toDegrees(centerLat),
            Math.toDegrees(centerLng)
        };
    }

    public double calculateDistance(
        double lat1, double lng1,
        double lat2, double lng2
    ) {
        final double R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) *
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
```

---

## Step 2: Kakao API 연동 (지하철역 검색)

### KakaoApiConfig.java

```java
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
```

### KakaoLocalResponse.java (record)

```java
package com.dnd.moyeolak.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoLocalResponse(
    @JsonProperty("documents")
    List<KakaoDocument> documents,

    @JsonProperty("meta")
    Meta meta
) {
    public record Meta(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("pageable_count")
        int pageableCount,

        @JsonProperty("is_end")
        boolean isEnd
    ) {}
}
```

### KakaoDocument.java (record)

```java
package com.dnd.moyeolak.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoDocument(
    @JsonProperty("place_name")
    String placeName,

    @JsonProperty("category_name")
    String categoryName,

    @JsonProperty("category_group_code")
    String categoryGroupCode,

    @JsonProperty("category_group_name")
    String categoryGroupName,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("address_name")
    String addressName,

    @JsonProperty("road_address_name")
    String roadAddressName,

    @JsonProperty("x")
    String x,  // 경도

    @JsonProperty("y")
    String y,  // 위도

    @JsonProperty("place_url")
    String placeUrl,

    @JsonProperty("distance")
    String distance  // 중심점으로부터 거리 (m)
) {}
```

### KakaoLocalClient.java

> **참고:** 프로젝트에 여러 `RestTemplate` Bean이 존재하므로 `@Qualifier("kakaoRestTemplate")`로 명시적 주입한다.

```java
package com.dnd.moyeolak.global.client.kakao;

import com.dnd.moyeolak.global.client.kakao.config.KakaoApiConfig;
import com.dnd.moyeolak.global.client.kakao.dto.KakaoLocalResponse;
import com.dnd.moyeolak.test.janghh.dto.response.SubwayStation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class KakaoLocalClient {

    private final RestTemplate kakaoRestTemplate;
    private final KakaoApiConfig kakaoApiConfig;

    private static final String KAKAO_LOCAL_API_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final String SUBWAY_CATEGORY_CODE = "SW8";

    public KakaoLocalClient(
        @Qualifier("kakaoRestTemplate") RestTemplate kakaoRestTemplate,
        KakaoApiConfig kakaoApiConfig
    ) {
        this.kakaoRestTemplate = kakaoRestTemplate;
        this.kakaoApiConfig = kakaoApiConfig;
    }

    public List<SubwayStation> findNearestSubwayStations(
        double centerLat,
        double centerLng,
        int radius,
        int limit
    ) {
        String url = String.format(
            "%s?category_group_code=%s&x=%f&y=%f&radius=%d&sort=distance",
            KAKAO_LOCAL_API_URL,
            SUBWAY_CATEGORY_CODE,
            centerLng,  // x = 경도
            centerLat,  // y = 위도
            radius
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiConfig.getKakaoApiKey());

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoLocalResponse> response = kakaoRestTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoLocalResponse.class
            );

            if (response.getBody() == null) {
                log.warn("Kakao Local API 응답이 비어있습니다.");
                return List.of();
            }

            return response.getBody()
                .documents()
                .stream()
                .limit(limit)
                .map(doc -> new SubwayStation(
                    doc.placeName(),
                    doc.addressName(),
                    doc.roadAddressName(),
                    Double.parseDouble(doc.y()),
                    Double.parseDouble(doc.x()),
                    Integer.parseInt(doc.distance())
                ))
                .toList();

        } catch (Exception e) {
            log.error("Kakao Local API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("지하철역 검색에 실패했습니다.", e);
        }
    }
}
```

---

## Step 3: ODsay API 연동 (이동시간 계산)

### OdsayApiConfig.java

```java
package com.dnd.moyeolak.global.client.odsay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OdsayApiConfig {

    @Value("${odsay.api.key}")
    private String odsayApiKey;

    public String getOdsayApiKey() {
        return odsayApiKey;
    }

    @Bean("odsayRestTemplate")
    public RestTemplate odsayRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
```

### OdsayPathResponse.java (record)

```java
package com.dnd.moyeolak.global.client.odsay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OdsayPathResponse(
    @JsonProperty("result")
    Result result
) {
    public record Result(
        @JsonProperty("path")
        List<Path> path
    ) {}

    public record Path(
        @JsonProperty("info")
        OdsayPathInfo info,

        @JsonProperty("subPath")
        List<OdsaySubPath> subPath
    ) {}
}
```

### OdsayPathInfo.java (record)

> **주의:** ODsay API는 일부 필드를 null로 반환할 수 있으므로 모든 숫자 필드를 `Integer`(래퍼 타입)로 선언하고, safe accessor 메서드를 통해 null-safe하게 접근한다.

```java
package com.dnd.moyeolak.global.client.odsay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OdsayPathInfo(
    @JsonProperty("totalTime")
    Integer totalTime,

    @JsonProperty("payment")
    Integer payment,

    @JsonProperty("busTransitCount")
    Integer busTransitCount,

    @JsonProperty("subwayTransitCount")
    Integer subwayTransitCount,

    @JsonProperty("totalDistance")
    Integer totalDistance,

    @JsonProperty("totalWalk")
    Integer totalWalk,

    @JsonProperty("totalStationCount")
    Integer totalStationCount
) {
    public int safeTotal() { return totalTime != null ? totalTime : 999; }
    public int safePayment() { return payment != null ? payment : 0; }
    public int safeBusTransit() { return busTransitCount != null ? busTransitCount : 0; }
    public int safeSubwayTransit() { return subwayTransitCount != null ? subwayTransitCount : 0; }
    public int safeTotalDistance() { return totalDistance != null ? totalDistance : 0; }
}
```

### OdsaySubPath.java (record)

```java
package com.dnd.moyeolak.global.client.odsay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OdsaySubPath(
    @JsonProperty("trafficType")
    Integer trafficType,

    @JsonProperty("distance")
    Integer distance,

    @JsonProperty("sectionTime")
    Integer sectionTime,

    @JsonProperty("stationCount")
    Integer stationCount,

    @JsonProperty("lane")
    List<Lane> lane,

    @JsonProperty("startName")
    String startName,

    @JsonProperty("endName")
    String endName
) {
    public record Lane(
        @JsonProperty("name")
        String name,

        @JsonProperty("subwayCode")
        Integer subwayCode
    ) {}
}
```

### OdsayClient.java

> **참고:** `UriComponentsBuilder.fromUriString()`으로 URL을 빌드하고, raw JSON을 `ObjectMapper`로 수동 파싱한다. Jackson 3.x 사용 프로젝트이므로 `tools.jackson.databind.ObjectMapper`를 import한다.

```java
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
            String rawResponse = odsayRestTemplate.getForObject(url, String.class);

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
```

---

## DTO 정의 (Java record)

### OptimalLocationRequest.java

```java
package com.dnd.moyeolak.test.janghh.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OptimalLocationRequest(
    @NotEmpty(message = "참가자 정보는 필수입니다.")
    List<ParticipantInfo> participants
) {
    public record ParticipantInfo(
        @NotNull(message = "참가자 이름은 필수입니다.")
        String name,

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude
    ) {}
}
```

### OptimalLocationResponse.java

```java
package com.dnd.moyeolak.test.janghh.dto.response;

import java.util.List;

public record OptimalLocationResponse(
    CenterPoint centerPoint,
    List<EvaluatedPlace> recommendations
) {}
```

### CenterPoint.java

```java
package com.dnd.moyeolak.test.janghh.dto.response;

public record CenterPoint(
    double latitude,
    double longitude
) {}
```

### SubwayStation.java

```java
package com.dnd.moyeolak.test.janghh.dto.response;

public record SubwayStation(
    String name,
    String address,
    String roadAddress,
    double latitude,
    double longitude,
    int distanceFromCenter  // 중간점으로부터 거리 (m)
) {}
```

### EvaluatedPlace.java

```java
package com.dnd.moyeolak.test.janghh.dto.response;

import java.util.List;

public record EvaluatedPlace(
    int rank,
    String name,
    String category,
    String address,
    double latitude,
    double longitude,
    int distanceFromCenter,
    int minSum,           // 총 이동시간 (분)
    int minMax,           // 최대 이동시간 (분)
    double avgDuration,   // 평균 이동시간 (분)
    List<RouteDetail> routes
) {}
```

### RouteDetail.java

```java
package com.dnd.moyeolak.test.janghh.dto.response;

public record RouteDetail(
    String participantName,
    int duration,       // 이동시간 (분)
    int distance,       // 이동거리 (m)
    int payment,        // 요금 (원)
    int transitCount    // 환승 횟수
) {}
```

---

## Service 구현

### OptimalLocationService.java

> **참고:** ODsay API 응답의 null 필드를 안전하게 처리하기 위해 `pathInfo.safeTotal()`, `pathInfo.safeTotalDistance()` 등 safe accessor 메서드를 사용한다.

```java
package com.dnd.moyeolak.test.janghh.service;

import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.odsay.OdsayClient;
import com.dnd.moyeolak.global.client.odsay.dto.OdsayPathInfo;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest.ParticipantInfo;
import com.dnd.moyeolak.test.janghh.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimalLocationService {

    private final KakaoLocalClient kakaoLocalClient;
    private final OdsayClient odsayClient;
    private final GeographicCalculator geographicCalculator;

    private static final int SEARCH_RADIUS = 5000;
    private static final int CANDIDATE_COUNT = 5;
    private static final int RECOMMENDATION_COUNT = 3;

    public OptimalLocationResponse findOptimalLocations(OptimalLocationRequest request) {
        List<ParticipantInfo> participants = request.participants();

        double[] center = geographicCalculator.calculateGeographicCenter(participants);
        log.info("중간지점 계산 완료: 위도={}, 경도={}", center[0], center[1]);

        List<SubwayStation> stations = kakaoLocalClient.findNearestSubwayStations(
            center[0],
            center[1],
            SEARCH_RADIUS,
            CANDIDATE_COUNT
        );

        if (stations.isEmpty()) {
            throw new IllegalStateException("주변에 지하철역이 없습니다.");
        }

        log.info("지하철역 검색 완료: {}개", stations.size());

        List<EvaluatedPlace> evaluatedPlaces = evaluatePlaces(participants, stations);

        List<EvaluatedPlace> recommendations = evaluatedPlaces.stream()
            .limit(RECOMMENDATION_COUNT)
            .toList();

        return new OptimalLocationResponse(
            new CenterPoint(center[0], center[1]),
            recommendations
        );
    }

    private List<EvaluatedPlace> evaluatePlaces(
        List<ParticipantInfo> participants,
        List<SubwayStation> stations
    ) {
        List<EvaluatedPlace> evaluated = new ArrayList<>();

        for (SubwayStation station : stations) {
            List<Integer> durations = new ArrayList<>();
            List<RouteDetail> routes = new ArrayList<>();

            for (ParticipantInfo participant : participants) {
                try {
                    OdsayPathInfo pathInfo = odsayClient.searchRoute(
                        participant.latitude(),
                        participant.longitude(),
                        station.latitude(),
                        station.longitude()
                    );

                    durations.add(pathInfo.safeTotal());

                    routes.add(new RouteDetail(
                        participant.name(),
                        pathInfo.safeTotal(),
                        pathInfo.safeTotalDistance(),
                        pathInfo.safePayment(),
                        pathInfo.safeBusTransit() + pathInfo.safeSubwayTransit()
                    ));

                    Thread.sleep(100);

                } catch (Exception e) {
                    log.error("경로 탐색 실패: {} -> {}", participant.name(), station.name(), e);
                    durations.add(999);
                }
            }

            int minSum = durations.stream().mapToInt(Integer::intValue).sum();
            int minMax = durations.stream().mapToInt(Integer::intValue).max().orElse(0);
            double avgDuration = durations.stream().mapToInt(Integer::intValue).average().orElse(0);

            evaluated.add(new EvaluatedPlace(
                0,
                station.name(),
                "지하철역",
                station.address(),
                station.latitude(),
                station.longitude(),
                station.distanceFromCenter(),
                minSum,
                minMax,
                avgDuration,
                routes
            ));
        }

        evaluated.sort(Comparator.comparingInt(EvaluatedPlace::minSum));

        List<EvaluatedPlace> rankedPlaces = new ArrayList<>();
        for (int i = 0; i < evaluated.size(); i++) {
            EvaluatedPlace place = evaluated.get(i);
            rankedPlaces.add(new EvaluatedPlace(
                i + 1,
                place.name(),
                place.category(),
                place.address(),
                place.latitude(),
                place.longitude(),
                place.distanceFromCenter(),
                place.minSum(),
                place.minMax(),
                place.avgDuration(),
                place.routes()
            ));
        }

        return rankedPlaces;
    }
}
```

---

## Controller 구현

### OptimalLocationController.java

> **참고:** 프로젝트 컨벤션에 따라 `ApiResponse<T>`로 응답을 래핑한다.

```java
package com.dnd.moyeolak.test.janghh.controller;

import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest;
import com.dnd.moyeolak.test.janghh.dto.response.OptimalLocationResponse;
import com.dnd.moyeolak.test.janghh.service.OptimalLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test/janghh/optimal-location")
@RequiredArgsConstructor
public class OptimalLocationController {

    private final OptimalLocationService optimalLocationService;

    @PostMapping
    public ApiResponse<OptimalLocationResponse> findOptimalLocations(
        @Valid @RequestBody OptimalLocationRequest request
    ) {
        log.info("최적 만남 장소 추천 요청: 참가자 {}명", request.participants().size());

        OptimalLocationResponse response = optimalLocationService.findOptimalLocations(request);

        log.info("최적 만남 장소 추천 완료: {}개 장소", response.recommendations().size());

        return ApiResponse.success(response);
    }
}
```

---

## application.yml 설정

```yaml
# Kakao API 설정
kakao:
  api:
    key: ${KAKAO_API_KEY}  # 환경변수로 관리

# ODsay API 설정
odsay:
  api:
    key: ${ODSAY_API_KEY}  # 환경변수로 관리
```

---

## 사용 예시

### Request

```http
POST /api/test/janghh/optimal-location
Content-Type: application/json

{
  "participants": [
    { "name": "김수아", "latitude": 37.497, "longitude": 127.027 },
    { "name": "이준수", "latitude": 37.566, "longitude": 126.978 },
    { "name": "박민지", "latitude": 37.513, "longitude": 127.100 },
    { "name": "최유진", "latitude": 37.500, "longitude": 127.035 },
    { "name": "정민호", "latitude": 37.520, "longitude": 127.045 },
    { "name": "강서연", "latitude": 37.510, "longitude": 127.060 }
  ]
}
```

### Response

```json
{
  "code": "OK",
  "message": "요청이 성공했습니다.",
  "data": {
    "centerPoint": {
      "latitude": 37.518,
      "longitude": 127.041
    },
    "recommendations": [
      {
        "rank": 1,
        "name": "강남역 2호선",
        "category": "지하철역",
        "address": "서울 강남구 역삼동 825",
        "latitude": 37.498,
        "longitude": 127.028,
        "distanceFromCenter": 523,
        "minSum": 180,
        "minMax": 40,
        "avgDuration": 30.0,
        "routes": [
          {
            "participantName": "김수아",
            "duration": 15,
            "distance": 5200,
            "payment": 1250,
            "transitCount": 0
          },
          {
            "participantName": "이준수",
            "duration": 35,
            "distance": 12800,
            "payment": 1400,
            "transitCount": 1
          }
        ]
      }
    ]
  }
}
```

---

## 평가 지표

### MinSum (총 이동시간 최소화)
```
MinSum = Σ(각 참가자의 이동시간)
```
**목표:** 전체 효율성 최대화

### MinMax (최대 이동시간 최소화)
```
MinMax = Max(각 참가자의 이동시간)
```
**목표:** 공정성 확보

### AvgDuration (평균 이동시간)
```
AvgDuration = MinSum / 참가자수
```
**목표:** 직관적 이해

---

## API 호출 횟수

**6명, 상위 5개 역 평가 시:**
- Kakao Local (지하철역 검색): 1회
- ODsay (경로 탐색): 5개 역 × 6명 = 30회
- **총 31회**

**Rate Limit 고려:**
- Kakao: 무료 일 300,000회 (충분)
- ODsay: 무료 일 1,000회 (충분)
- ODsay 호출 간 100ms sleep으로 Rate Limit 방지

---

## 트러블슈팅 이력

### 1. ODsay API null 필드 역직렬화 오류
- **문제:** `MismatchedInputException: Cannot map null into type int`
- **원인:** ODsay API가 일부 숫자 필드를 null로 반환
- **해결:** `OdsayPathInfo`, `OdsaySubPath`의 `int` → `Integer` 변환 + safe accessor 추가

### 2. ODsay URL 빌딩 이슈
- **문제:** `String.format("%f")`가 소수점 이하 불필요한 자릿수 생성
- **해결:** `UriComponentsBuilder.fromUriString()`으로 URL 빌딩 방식 변경

### 3. Jackson 3.x 호환성
- **문제:** `com.fasterxml.jackson.databind` 패키지를 찾을 수 없음
- **원인:** 프로젝트가 Jackson 3.x 사용 (`tools.jackson.core` 패키지)
- **해결:** `tools.jackson.databind.ObjectMapper` import 사용

---

## 향후 개선 사항

1. **캐싱**: 동일 출발지-도착지는 Redis 캐싱
2. **병렬 처리**: CompletableFuture로 API 호출 병렬화
3. **다양한 장소**: 카페, 식당 등 추가
4. **가중치**: MinSum + (α × MinMax) 혼합 점수

---

작성일: 2025-01-31
작성자: 모여락 개발팀
문서 버전: 2.0
