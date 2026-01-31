package com.dnd.moyeolak.test.janghh.dto.response;

public record SubwayStation(
    String name,
    String address,
    String roadAddress,
    double latitude,
    double longitude,
    int distanceFromCenter
) {}
