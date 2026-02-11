package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "중간지점 추천 결과")
public record MidpointRecommendationResponse(
        @Schema(description = "모든 참여자의 중심 좌표")
        CenterPointDto centerPoint,

        @Schema(description = "추천 지하철역 목록 (최대 3개, 평균 이동시간 오름차순)")
        List<StationRecommendationDto> recommendations
) {}
