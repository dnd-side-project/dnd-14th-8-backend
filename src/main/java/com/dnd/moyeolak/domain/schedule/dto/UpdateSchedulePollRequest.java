package com.dnd.moyeolak.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "일정 투표 옵션 수정 요청")
public record UpdateSchedulePollRequest(

        @Schema(
                description = "투표 가능한 날짜 목록 (최소 1개 이상, 중복 불가)",
                example = "[\"2025-02-10\", \"2025-02-11\", \"2025-02-12\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty List<@NotNull LocalDate> dateOptions,

        @Schema(
                description = "투표 시작 시간 (HH:mm, 30분 단위만 허용, endTime보다 작아야 함)",
                example = "07:00",
                pattern = "^(?:[01]\\\\d|2[0-3]):(?:00|30)$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Pattern(regexp = "^(?:[01]\\d|2[0-3]):(?:00|30)$")
        String startTime,

        @Schema(
                description = "투표 종료 시간 (HH:mm 또는 24:00, 30분 단위만 허용, startTime보다 커야 함)",
                example = "24:00",
                pattern = "^(?:24:00|(?:[01]\\\\d|2[0-3]):(?:00|30))$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Pattern(regexp = "^(?:24:00|(?:[01]\\d|2[0-3]):(?:00|30))$")
        String endTime
) {
}
