package com.dnd.moyeolak.domain.location.service.impl;

import com.dnd.moyeolak.domain.location.dto.NearbyPlaceSearchResponse;
import com.dnd.moyeolak.domain.location.entity.NearbyPlace;
import com.dnd.moyeolak.domain.location.entity.NearbyPlaceHours;
import com.dnd.moyeolak.domain.location.entity.enums.PlaceCategory;
import com.dnd.moyeolak.domain.location.repository.NearbyPlaceRepository;
import com.dnd.moyeolak.domain.location.service.BusinessHoursCalculator;
import com.dnd.moyeolak.domain.location.service.BusinessHoursCalculator.BusinessStatus;
import com.dnd.moyeolak.domain.location.service.NearbyPlaceSearchService;
import com.dnd.moyeolak.global.client.dto.CombinedPlaceResponse;
import com.dnd.moyeolak.global.client.google.GooglePlacesClient;
import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse;
import com.dnd.moyeolak.global.client.google.dto.GoogleTextSearchRequest;
import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;
import com.dnd.moyeolak.global.client.kakao.dto.KakaoKeywordSearchRequest;
import com.dnd.moyeolak.global.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NearbyPlaceSearchServiceImpl implements NearbyPlaceSearchService {

    private final GooglePlacesClient googlePlacesClient;
    private final KakaoLocalClient kakaoLocalClient;
    private final NearbyPlaceRepository nearbyPlaceRepository;
    private final BusinessHoursCalculator businessHoursCalculator;

    @Override
    public NearbyPlaceSearchResponse nearbyPlaceSearch(String latitude, String longitude) {
        BigDecimal baseLat = new BigDecimal(latitude);
        BigDecimal baseLng = new BigDecimal(longitude);
        double latDouble = baseLat.doubleValue();
        double lngDouble = baseLng.doubleValue();

        // 1. DB 캐시 조회 (자체 짧은 tx)
        List<NearbyPlace> cached = nearbyPlaceRepository.findByBaseLatitudeAndBaseLongitude(baseLat, baseLng);

        if (!cached.isEmpty()) {
            NearbyPlace sample = cached.getFirst();
            LocalDateTime updatedAt = sample.getUpdatedAt();
            if (updatedAt.plusDays(30).isAfter(LocalDateTime.now())) {
                return buildResponse(cached);
            }
            // 캐시 만료 → 삭제 (자체 짧은 tx)
            nearbyPlaceRepository.deleteByBaseLatitudeAndBaseLongitude(baseLat, baseLng);
        }

        // 2. API 호출 (tx 없음, DB 커넥션 미점유)
        Map<PlaceCategory, List<CombinedPlaceResponse.CombinedPlace>> result = fetchFromApis(latDouble, lngDouble);

        // 3. 엔티티 변환 + DB 저장 (카테고리 간 googlePlaceId 중복 제거)
        List<NearbyPlace> allPlaces = new ArrayList<>();
        Set<String> seenGooglePlaceIds = new HashSet<>();

        for (Map.Entry<PlaceCategory, List<CombinedPlaceResponse.CombinedPlace>> entry : result.entrySet()) {
            PlaceCategory category = entry.getKey();

            for (CombinedPlaceResponse.CombinedPlace combined : entry.getValue()) {
                if (!seenGooglePlaceIds.add(combined.googlePlaceId())) {
                    continue;
                }
                NearbyPlace nearbyPlace = NearbyPlace.of(
                        baseLat,
                        baseLng,
                        category,
                        combined.googlePlaceId(),
                        combined.name(),
                        combined.formattedAddress(),
                        combined.latitude() != null ? BigDecimal.valueOf(combined.latitude()) : null,
                        combined.longitude() != null ? BigDecimal.valueOf(combined.longitude()) : null,
                        combined.kakaoPlaceUrl(),
                        (int) combined.distanceFromBase()
                );

                if (combined.regularOpeningHours() != null && combined.regularOpeningHours().periods() != null) {
                    for (GooglePlacesResponse.Period period : combined.regularOpeningHours().periods()) {
                        if (period.open() == null || period.close() == null) continue;

                        NearbyPlaceHours hours = NearbyPlaceHours.of(
                                nearbyPlace,
                                period.open().day(),
                                period.open().hour(),
                                period.open().minute(),
                                period.close().day(),
                                period.close().hour(),
                                period.close().minute()
                        );
                        nearbyPlace.addHours(hours);
                    }
                }

                allPlaces.add(nearbyPlace);
            }
        }

        // DB 저장 (자체 짧은 tx)
        nearbyPlaceRepository.saveAll(allPlaces);

        // 4. 응답 반환
        return buildResponse(allPlaces);
    }

    private Map<PlaceCategory, List<CombinedPlaceResponse.CombinedPlace>> fetchFromApis(double latitude, double longitude) {
        Map<PlaceCategory, List<CombinedPlaceResponse.CombinedPlace>> result = new LinkedHashMap<>();

        // 모든 카테고리의 키워드를 수집
        List<String> allKeywords = Arrays.stream(PlaceCategory.values())
                .flatMap(cat -> cat.getSearchKeywords().stream())
                .toList();

        Map<String, List<GooglePlacesResponse.Place>> keywordResults = new ConcurrentHashMap<>();

        // Google Text Search (virtual threads)
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> googleFutures = allKeywords.stream()
                    .map(keyword -> CompletableFuture.runAsync(() -> {
                        GooglePlacesResponse response = googlePlacesClient.searchText(
                                GoogleTextSearchRequest.ofDistanceSorted(keyword, latitude, longitude)
                        );
                        keywordResults.put(keyword, response.places() != null ? response.places() : List.of());
                    }, executor))
                    .toList();

            CompletableFuture.allOf(googleFutures.toArray(CompletableFuture[]::new)).join();
        }

        // 카테고리별 병합 + Google Place ID 중복 제거
        Map<PlaceCategory, List<GooglePlacesResponse.Place>> categoryPlaces = new LinkedHashMap<>();

        for (PlaceCategory category : PlaceCategory.values()) {
            Set<String> seenIds = new HashSet<>();
            List<GooglePlacesResponse.Place> merged = new ArrayList<>();

            for (String keyword : category.getSearchKeywords()) {
                List<GooglePlacesResponse.Place> places = keywordResults.getOrDefault(keyword, List.of());
                for (GooglePlacesResponse.Place place : places) {
                    if (place.id() != null && seenIds.add(place.id())) {
                        merged.add(place);
                    }
                }
            }

            categoryPlaces.put(category, merged);
        }

        // Kakao 검증 (카테고리당 1 virtual thread)
        record VerifyResult(PlaceCategory category, List<CombinedPlaceResponse.CombinedPlace> verified) {}

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<VerifyResult>> verifyFutures = categoryPlaces.entrySet().stream()
                    .map(entry -> CompletableFuture.supplyAsync(() -> {
                        PlaceCategory category = entry.getKey();
                        List<CombinedPlaceResponse.CombinedPlace> verified = new ArrayList<>();

                        for (GooglePlacesResponse.Place googlePlace : entry.getValue()) {
                            String placeName = googlePlace.displayName() != null ? googlePlace.displayName().text() : null;
                            if (placeName == null || googlePlace.location() == null) continue;

                            KakaoKeywordSearchRequest kakaoRequest = KakaoKeywordSearchRequest.of(
                                    placeName,
                                    String.valueOf(googlePlace.location().longitude()),
                                    String.valueOf(googlePlace.location().latitude())
                            );

                            try {
                                CategorySearchResponse kakaoResponse = kakaoLocalClient.searchByKeyword(kakaoRequest);

                                if (kakaoResponse.documents() == null || kakaoResponse.documents().isEmpty()) {
                                    continue;
                                }

                                long distance = Math.round(GeoUtils.haversine(
                                        latitude, longitude, googlePlace.location().latitude(), googlePlace.location().longitude()
                                ));

                                verified.add(CombinedPlaceResponse.CombinedPlace.of(
                                        googlePlace,
                                        kakaoResponse.documents().getFirst(),
                                        distance
                                ));
                            } catch (Exception e) {
                                // Kakao 검증 실패 시 해당 장소 스킵
                                log.warn("Kakao API 검증에 실패했습니다. 장소명: '{}', 오류: {}", placeName, e.getMessage());
                            }
                        }

                        return new VerifyResult(category, verified);
                    }, executor))
                    .toList();

            for (CompletableFuture<VerifyResult> future : verifyFutures) {
                VerifyResult vr = future.join();
                result.put(vr.category(), vr.verified());
            }
        }

        return result;
    }

    private NearbyPlaceSearchResponse buildResponse(List<NearbyPlace> places) {
        Map<PlaceCategory, List<NearbyPlace>> grouped = new LinkedHashMap<>();
        for (PlaceCategory cat : PlaceCategory.values()) {
            grouped.put(cat, new ArrayList<>());
        }
        for (NearbyPlace place : places) {
            grouped.get(place.getCategory()).add(place);
        }

        List<NearbyPlaceSearchResponse.CategoryPlaces> categories = new ArrayList<>();

        for (Map.Entry<PlaceCategory, List<NearbyPlace>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            List<NearbyPlaceSearchResponse.PlaceDetail> placeDetails = entry.getValue().stream()
                    .map(place -> {
                        BusinessStatus status = businessHoursCalculator.calculateBusinessStatus(place.getNearbyPlaceHours());
                        return new NearbyPlaceSearchResponse.PlaceDetail(
                                place.getId(),
                                place.getName(),
                                place.getFormattedAddress(),
                                place.getLatitude() != null ? place.getLatitude().doubleValue() : null,
                                place.getLongitude() != null ? place.getLongitude().doubleValue() : null,
                                place.getKakaoPlaceUrl(),
                                place.getDistanceFromBase(),
                                status.isOpen(),
                                status.message()
                        );
                    })
                    .toList();

            categories.add(new NearbyPlaceSearchResponse.CategoryPlaces(
                    entry.getKey().getDisplayName(), placeDetails));
        }

        return new NearbyPlaceSearchResponse(categories);
    }
}
