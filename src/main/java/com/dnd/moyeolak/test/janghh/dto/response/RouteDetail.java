package com.dnd.moyeolak.test.janghh.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참가자별 이동 경로 요약")
public record RouteDetail(
    @Schema(description = "참가자 이름", example = "참여자1")
    String participantName,

    @Schema(description = "소요 시간(분)", example = "28")
    int duration,

    @Schema(description = "이동 거리(미터)", example = "16128")
    int distance,

    @Schema(description = "요금(원)", example = "1750")
    int payment,

    @Schema(description = "환승 횟수", example = "2")
    int transitCount
) {}
