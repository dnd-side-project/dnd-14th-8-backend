package com.dnd.moyeolak.global.client.google.dto;

public record GoogleTextSearchRequest(
        String textQuery,
        String languageCode,
        Integer maxResultCount,
        LocationBias locationBias,
        String rankPreference    // DISTANCE or RELEVANCE
) {
    private final static int DEFAULT_RESULT_COUNT = 20;
    private final static Double DEFAULT_RADIUS_METERS = 1000.0;

    public static GoogleTextSearchRequest ofDistanceSorted(String textQuery, Double latitude, Double longitude) {
        return ofDistanceSorted(textQuery, latitude, longitude, DEFAULT_RADIUS_METERS, DEFAULT_RESULT_COUNT);
    }

    public static GoogleTextSearchRequest ofDistanceSorted(String textQuery, Double latitude, Double longitude, Double radiusMeters) {
        return ofDistanceSorted(textQuery, latitude, longitude, radiusMeters, DEFAULT_RESULT_COUNT);
    }

    public static GoogleTextSearchRequest ofDistanceSorted(String textQuery, Double latitude, Double longitude, Double radiusMeters, Integer maxResultCount) {
        LocationBias locationBias = new LocationBias(
                new Circle(new Center(latitude, longitude), radiusMeters)
        );
        return new GoogleTextSearchRequest(textQuery, "ko", maxResultCount, locationBias, "DISTANCE");
    }

    public record LocationBias(Circle circle) {}

    public record Circle(Center center, Double radius) {}

    public record Center(Double latitude, Double longitude) {}
}
