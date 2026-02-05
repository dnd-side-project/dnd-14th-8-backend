package com.dnd.moyeolak.domain.participant.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일정 기반 참여자 생성 요청")
public record CreateParticipantWithScheduleRequest(
        @Schema(description = "참여자 이름", example = "김철수")
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
        String name,

        @Schema(description = "브라우저 식별 키 (UUID 권장)", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "localStorageKey는 필수입니다")
        @Size(max = 100, message = "localStorageKey는 100자를 초과할 수 없습니다")
        String localStorageKey,

        @ArraySchema(
                schema = @Schema(description = "가능한 시간 (30분 단위)"),
                arraySchema = @Schema(
                        description = "참여 가능한 시간대 목록",
                        example = "[\"2025-02-10T09:00:00\", \"2025-02-10T09:30:00\", \"2025-02-10T10:00:00\", \"2025-02-10T14:00:00\"]"
                )
        )
        @NotEmpty(message = "가능한 시간 정보는 필수입니다")
        List<@NotNull LocalDateTime> availableSchedules
) {
}
