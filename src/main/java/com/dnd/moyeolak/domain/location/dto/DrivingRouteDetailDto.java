package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "자동차 경로 상세 정보")
public record DrivingRouteDetailDto(
        @Schema(description = "이동 시간(분)", example = "58")
        int durationMinutes,
        @Schema(description = "이동 거리(미터)", example = "12500")
        int distanceMeters,
        @Schema(description = "톨게이트 요금(원)", example = "5000")
        int tollFare,
        @Schema(description = "택시 요금 추정치(원)", example = "55800")
        int estimatedTaxiFare
) {
}
