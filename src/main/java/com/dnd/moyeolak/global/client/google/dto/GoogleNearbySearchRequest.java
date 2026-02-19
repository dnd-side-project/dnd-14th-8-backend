package com.dnd.moyeolak.global.client.google.dto;

import java.util.List;

public record GoogleNearbySearchRequest(
        List<String> includedTypes,
        List<String> excludedTypes,
        Integer maxResultCount,
        String rankPreference,        // DISTANCE or POPULARITY
        String languageCode,
        LocationRestriction locationRestriction
) {
    public static GoogleNearbySearchRequest ofDistanceSorted(String type, Double latitude, Double longitude, Double radiusMeters) {
        return ofDistanceSorted(type, latitude, longitude, radiusMeters, 20);
    }

    public static GoogleNearbySearchRequest ofDistanceSorted(String type, Double latitude, Double longitude, Double radiusMeters, Integer maxResultCount) {
        return new GoogleNearbySearchRequest(
                List.of(type),
                null,
                maxResultCount,
                "DISTANCE",
                "ko",
                new LocationRestriction(new Circle(new Center(latitude, longitude), radiusMeters))
        );
    }

    public record LocationRestriction(Circle circle) {}

    public record Circle(Center center, Double radius) {}

    public record Center(Double latitude, Double longitude) {}
}
