package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "추천 지하철역 정보")
@Builder
public record StationRecommendationDto(
        @Schema(description = "추천 순위 (1=최적)", example = "1")
        int rank,

        @Schema(description = "역 ID", example = "321")
        Long stationId,

        @Schema(description = "지하철역 이름", example = "디지털미디어시티역")
        String stationName,

        @Schema(description = "노선명", example = "경의중앙선")
        String line,

        @Schema(description = "역 위도", example = "37.5779")
        double latitude,

        @Schema(description = "역 경도", example = "126.8997")
        double longitude,

        @Schema(description = "중심점과의 거리 (미터)", example = "1200")
        int distanceFromCenter,

        @Schema(description = "평균 대중교통 이동시간 (분)", example = "92")
        double avgTransitDuration,

        @Schema(description = "각 참여자의 이동 경로 목록")
        List<RouteDto> routes
) {}
