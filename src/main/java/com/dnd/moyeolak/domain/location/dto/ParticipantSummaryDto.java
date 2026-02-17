package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상세 경로 요청 참여자 정보")
public record ParticipantSummaryDto(
        @Schema(description = "참여자 ID", example = "15")
        Long participantId,
        @Schema(description = "참여자 이름", example = "김혜인")
        String participantName,
        @Schema(description = "등록된 출발지 주소", example = "서울특별시 강서구 화곡동")
        String departureAddress
) {
}
