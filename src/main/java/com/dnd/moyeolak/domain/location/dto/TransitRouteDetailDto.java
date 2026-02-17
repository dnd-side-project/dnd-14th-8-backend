package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대중교통 경로 상세 정보")
public record TransitRouteDetailDto(
        @Schema(description = "이동 시간(분)", example = "92")
        int durationMinutes,
        @Schema(description = "이동 거리(미터)", example = "21400")
        int distanceMeters,
        @Schema(description = "요금(원)", example = "5800")
        int fare,
        @Schema(description = "환승 횟수", example = "3")
        int transferCount,
        @Schema(description = "도보 거리(미터)", example = "820")
        int walkDistanceMeters
) {
}
