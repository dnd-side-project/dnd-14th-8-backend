package com.dnd.moyeolak.domain.meeting.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMeetingRequest(
        int participantCount,

        @NotBlank(message = "로컬스토리지 키는 필수입니다")
        String localStorageKey,

        @NotBlank(message = "참가자 이름은 필수입니다")
        String participantName
) {
}
