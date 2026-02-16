package com.dnd.moyeolak.domain.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateMeetingRequest(
        @Schema(description = "모임의 PK", example = "t3fZ_kLpQ9xNwR2hJmYaO")
        @NotBlank
        String meetingId,

        @Schema(description = "참가자 수", example = "5")
        @Min(value = 2, message = "참가자 수는 최소 2명 이상이어야 합니다")
        int participantCount,

        @Schema(description = "호스트 여부")
        boolean isHost
) {
}
