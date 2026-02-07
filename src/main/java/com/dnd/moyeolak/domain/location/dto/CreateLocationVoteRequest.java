package com.dnd.moyeolak.domain.location.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLocationVoteRequest(
    @NotBlank String meetingId,
    @NotBlank String locationPollId,
    String localStorageKey,
    @NotBlank String participantName,
    @NotBlank String departureLocation,
    @NotBlank String departureLat,
    @NotBlank String departureLng
) {
}
