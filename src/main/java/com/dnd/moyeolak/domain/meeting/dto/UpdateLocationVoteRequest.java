package com.dnd.moyeolak.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLocationVoteRequest(
        @NotBlank String participantName,
        @NotBlank String departureLocation,
        @NotBlank String departureLat,
        @NotBlank String departureLng
) {
}
