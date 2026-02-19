package com.dnd.moyeolak.global.client.dto;

import com.dnd.moyeolak.global.client.google.dto.GooglePlacesResponse;
import com.dnd.moyeolak.global.client.kakao.dto.CategorySearchResponse;

import java.util.List;

public record CombinedPlaceResponse(
        List<CombinedPlace> places
) {
    public record CombinedPlace(
            // Google 데이터
            String googlePlaceId,
            String name,
            String formattedAddress,
            Double latitude,
            Double longitude,
            GooglePlacesResponse.RegularOpeningHours regularOpeningHours,

            // Kakao 데이터
            String kakaoPlaceUrl,

            // 계산 데이터 - 기준점으로부터의 거리 (미터, Haversine 계산)
            long distanceFromBase
    ) {
        public static CombinedPlace of(GooglePlacesResponse.Place googlePlace, CategorySearchResponse.Place kakaoPlace, long distanceFromBase) {
            return new CombinedPlace(
                    googlePlace.id(),
                    googlePlace.displayName() != null ? googlePlace.displayName().text() : null,
                    googlePlace.formattedAddress(),
                    googlePlace.location() != null ? googlePlace.location().latitude() : null,
                    googlePlace.location() != null ? googlePlace.location().longitude() : null,
                    googlePlace.regularOpeningHours(),
                    kakaoPlace.placeUrl(),
                    distanceFromBase
            );
        }
    }
}
