package com.dnd.moyeolak.global.client.dto;

import com.dnd.moyeolak.global.client.google.dto.TextSearchResponse;
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
            String googleMapsUri,
            List<String> types,
            List<TextSearchResponse.Photo> photos,
            TextSearchResponse.RegularOpeningHours regularOpeningHours,
            List<TextSearchResponse.Review> reviews,
            TextSearchResponse.GenerativeSummary generativeSummary,
            TextSearchResponse.EditorialSummary editorialSummary,

            // Kakao 데이터
            String kakaoPlaceId,
            String kakaoPlaceUrl,
            String distance,
            String phone,
            String categoryName
    ) {
        public static CombinedPlace of(TextSearchResponse.Place googlePlace, CategorySearchResponse.Place kakaoPlace) {
            return new CombinedPlace(
                    // Google
                    googlePlace.id(),
                    googlePlace.displayName() != null ? googlePlace.displayName().text() : null,
                    googlePlace.formattedAddress(),
                    googlePlace.location() != null ? googlePlace.location().latitude() : null,
                    googlePlace.location() != null ? googlePlace.location().longitude() : null,
                    googlePlace.googleMapsUri(),
                    googlePlace.types(),
                    googlePlace.photos(),
                    googlePlace.regularOpeningHours(),
                    googlePlace.reviews(),
                    googlePlace.generativeSummary(),
                    googlePlace.editorialSummary(),

                    // Kakao
                    kakaoPlace.id(),
                    kakaoPlace.placeUrl(),
                    kakaoPlace.distance(),
                    kakaoPlace.phone(),
                    kakaoPlace.categoryName()
            );
        }
    }
}
