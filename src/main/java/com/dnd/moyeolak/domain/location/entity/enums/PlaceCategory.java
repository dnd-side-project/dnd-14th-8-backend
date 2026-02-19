package com.dnd.moyeolak.domain.location.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
    STUDY_CAFE("스터디카페", List.of("스터디카페")),
    MEETING_ROOM("회의실", List.of("회의실")),
    CAFE("카페", List.of("분위기좋은카페")),
    RESTAURANT("음식점", List.of("분위기좋은식당"));

    private final String displayName;
    private final List<String> searchKeywords;
}
