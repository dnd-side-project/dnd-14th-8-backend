package com.dnd.moyeolak.domain.location.dto;

public record CreateLocationVoteRequest(
    String meetingId,
    String locationPollId,
    String localStorageKey,
    String participantName,
    String departureLocation,
    String departureLat,
    String departureLng
) {
}
