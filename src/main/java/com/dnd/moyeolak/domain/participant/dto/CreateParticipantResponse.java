package com.dnd.moyeolak.domain.participant.dto;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "참여자 생성 응답")
public record CreateParticipantResponse(
        @Schema(description = "참여자 ID", example = "1")
        Long participantId,

        @Schema(description = "참여자 이름", example = "김철수")
        String name,

        @Schema(description = "일정 투표 수 (일정 기반 생성 시)", example = "3")
        Integer scheduleVoteCount,

        @Schema(description = "위치 정보 보유 여부 (위치 기반 생성 시)", example = "true")
        Boolean hasLocation,

        @Schema(description = "생성 일시", example = "2025-02-05T10:30:00")
        LocalDateTime createdAt
) {
    public static CreateParticipantResponse fromLocation(Participant participant) {
        return new CreateParticipantResponse(
                participant.getId(),
                participant.getName(),
                null,
                true,
                participant.getCreatedAt()
        );
    }
}
