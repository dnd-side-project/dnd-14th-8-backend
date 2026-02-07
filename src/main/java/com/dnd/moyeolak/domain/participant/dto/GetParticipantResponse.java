package com.dnd.moyeolak.domain.participant.dto;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여자 단건 조회 응답")
public record GetParticipantResponse(
        @Schema(description = "참여자 ID", example = "1")
        Long participantId,

        @Schema(description = "참여자 이름", example = "김철수")
        String name,

        @Schema(description = "방장 여부", example = "true")
        boolean isHost
) {
    public static GetParticipantResponse from(Participant participant) {
        return new GetParticipantResponse(
                participant.getParticipantId(),
                participant.getName(),
                participant.isHost()
        );
    }
}
