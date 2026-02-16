package com.dnd.moyeolak.domain.participant.dto;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import io.swagger.v3.oas.annotations.media.Schema;

public record ParticipantResponse(
        @Schema(description = "참여자 ID", example = "1")
        Long participantId,

        @Schema(description = "참여자 고유 세션 키", example = "김철수")
        String localStorageKey,

        @Schema(description = "참여자 이름", example = "김철수")
        String name,

        @Schema(description = "일정 투표 ID", example = "1")
        Long scheduleVoteId,

        @Schema(description = "위치 투표 ID", example = "1")
        Long locationVoteId,

        @Schema(description = "방장 여부", example = "true")
        boolean isHost
) {
    public static ParticipantResponse of(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getLocalStorageKey(),
                participant.getName(),
                participant.getScheduleVotes().isEmpty() ? null : participant.getScheduleVotes().getFirst().getId(),
                participant.getLocationVotes().isEmpty() ? null : participant.getLocationVotes().getFirst().getId(),
                participant.isHost()
        );
    }
}
