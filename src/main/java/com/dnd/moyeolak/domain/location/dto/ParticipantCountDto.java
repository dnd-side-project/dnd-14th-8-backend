package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여자 출발지 등록 현황")
public record ParticipantCountDto(
        @Schema(description = "출발지를 등록한 참여자 수", example = "3")
        int registeredCount,
        @Schema(description = "전체 참여자 수", example = "10")
        int totalCount
) {}
