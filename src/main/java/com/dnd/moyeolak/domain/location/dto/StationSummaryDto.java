package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상세 경로 대상 역 정보")
public record StationSummaryDto(
        @Schema(description = "역 ID", example = "321")
        Long stationId,
        @Schema(description = "역 이름", example = "김포공항역")
        String stationName,
        @Schema(description = "노선명", example = "5호선")
        String line
) {
}
