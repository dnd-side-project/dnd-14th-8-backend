package com.dnd.moyeolak.domain.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "중간지점 상세 경로 응답")
public record PersonalRouteResponse(
        @Schema(description = "참여자 정보")
        ParticipantSummaryDto participant,
        @Schema(description = "추천 역 정보")
        StationSummaryDto station,
        @Schema(description = "출발 시간 (미입력 시 null)", example = "2026-02-18T10:30:00")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        LocalDateTime departureTime,
        @Schema(description = "대중교통 경로 정보")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        TransitRouteDetailDto transit,
        @Schema(description = "자동차 경로 정보")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        DrivingRouteDetailDto driving
) {
}
