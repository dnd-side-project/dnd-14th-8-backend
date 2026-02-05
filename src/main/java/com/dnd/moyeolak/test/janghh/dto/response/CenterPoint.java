package com.dnd.moyeolak.test.janghh.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참가자들의 중심 좌표")
public record CenterPoint(
    @Schema(description = "중심점 위도", example = "37.54217301784178")
    double latitude,

    @Schema(description = "중심점 경도", example = "126.93569223930636")
    double longitude
) {}
