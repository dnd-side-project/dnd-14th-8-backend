package com.dnd.moyeolak.global.client.google.dto;

import java.util.List;

public record NearbySearchRequest(
        List<String> includedTypes,
        List<String> excludedTypes,
        Integer maxResultCount,
        String rankPreference,        // DISTANCE or POPULARITY
        String languageCode,
        LocationRestriction locationRestriction
) {
    public static NearbySearchRequest of(String type, Double latitude, Double longitude, Double radiusMeters) {
        return new NearbySearchRequest(
                List.of(type),
                null,
                null,
                null,
                "ko",
                new LocationRestriction(new Circle(new Center(latitude, longitude), radiusMeters))
        );
    }

    public static NearbySearchRequest of(List<String> types, Double latitude, Double longitude, Double radiusMeters) {
        return new NearbySearchRequest(
                types,
                null,
                null,
                null,
                "ko",
                new LocationRestriction(new Circle(new Center(latitude, longitude), radiusMeters))
        );
    }

    public static NearbySearchRequest ofDistanceSorted(String type, Double latitude, Double longitude, Double radiusMeters) {
        return new NearbySearchRequest(
                List.of(type),
                null,
                null,
                "DISTANCE",
                "ko",
                new LocationRestriction(new Circle(new Center(latitude, longitude), radiusMeters))
        );
    }

    public static NearbySearchRequest ofDistanceSorted(List<String> types, Double latitude, Double longitude, Double radiusMeters, Integer maxResultCount) {
        return new NearbySearchRequest(
                types,
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
