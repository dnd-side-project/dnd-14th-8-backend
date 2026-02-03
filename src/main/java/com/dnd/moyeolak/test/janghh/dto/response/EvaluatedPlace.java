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
    int minSum,
    int minMax,
    double avgDuration,
    List<RouteDetail> routes
) {}
