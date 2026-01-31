package com.dnd.moyeolak.global.client.google.dto;

public record TextSearchRequest(
        String textQuery,
        String languageCode,
        Integer maxResultCount,
        LocationBias locationBias,
        String rankPreference    // DISTANCE or RELEVANCE
) {
    public static TextSearchRequest of(String textQuery) {
        return new TextSearchRequest(textQuery, "ko", null, null, null);
    }

    public static TextSearchRequest of(String textQuery, Integer maxResultCount) {
        return new TextSearchRequest(textQuery, "ko", maxResultCount, null, null);
    }

    public static TextSearchRequest of(String textQuery, Double latitude, Double longitude, Double radiusMeters) {
        LocationBias locationBias = new LocationBias(
                new Circle(new Center(latitude, longitude), radiusMeters)
        );
        return new TextSearchRequest(textQuery, "ko", null, locationBias, null);
    }

    public static TextSearchRequest ofDistanceSorted(String textQuery, Double latitude, Double longitude, Double radiusMeters) {
        LocationBias locationBias = new LocationBias(
                new Circle(new Center(latitude, longitude), radiusMeters)
        );
        return new TextSearchRequest(textQuery, "ko", null, locationBias, "DISTANCE");
    }

    public record LocationBias(Circle circle) {}

    public record Circle(Center center, Double radius) {}

    public record Center(Double latitude, Double longitude) {}
}
