package com.dnd.moyeolak.domain.participant.dto;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "참여자 전체 조회 응답")
public record ListParticipantResponse(
        @Schema(description = "참여자 목록")
        List<ParticipantInfo> participants,

        @Schema(description = "총 참여자 수", example = "3")
        int totalCount
) {
    @Schema(description = "참여자 정보")
    public record ParticipantInfo(
            @Schema(description = "참여자 ID", example = "1")
            Long participantId,

            @Schema(description = "참여자 이름", example = "김철수")
            String name,

            @Schema(description = "참여자 세션키", example = "FGDNCEW@ASDDNJMC")
            String localStorageKey,

            @Schema(description = "방장 여부", example = "false")
            boolean isHost
    ) {
        public static ParticipantInfo from(Participant participant) {
            return new ParticipantInfo(
                    participant.getId(),
                    participant.getName(),
                    participant.getLocalStorageKey(),
                    participant.isHost()
            );
        }
    }

    public static ListParticipantResponse from(List<Participant> participants) {
        List<ParticipantInfo> infos = participants.stream()
                .map(ParticipantInfo::from)
                .toList();
        return new ListParticipantResponse(infos, infos.size());
    }
}
