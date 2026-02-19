package com.dnd.moyeolak.domain.location.dto;

import java.util.List;

public record NearbyPlaceSearchResponse(
        List<CategoryPlaces> categories
) {
    public record CategoryPlaces(
            String category,
            List<PlaceDetail> places
    ) {}

    public record PlaceDetail(
            Long id,
            String name,
            String formattedAddress,
            Double latitude,
            Double longitude,
            String kakaoPlaceUrl,
            Integer distanceFromBase,
            Boolean isOpen,
            String businessStatusMessage
    ) {}
}
