package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참가자들의 중심 좌표")
public record CenterPointDto(
        @Schema(description = "중심점 위도", example = "37.5665")
        double latitude,

        @Schema(description = "중심점 경도", example = "126.9780")
        double longitude
) {}
