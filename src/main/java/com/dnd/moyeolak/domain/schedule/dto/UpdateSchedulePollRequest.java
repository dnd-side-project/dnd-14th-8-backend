package com.dnd.moyeolak.domain.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
                description = "투표 시작 시간 (0~23시, endTime보다 작아야 함)",
                example = "7",
                minimum = "0",
                maximum = "23",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(0) @Max(23) int startTime,

        @Schema(
                description = "투표 종료 시간 (1~24시, startTime보다 커야 함. 24 = 자정)",
                example = "24",
                minimum = "1",
                maximum = "24",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(1) @Max(24) int endTime
) {
}
