package com.dnd.moyeolak.domain.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "출발지 수정 요청")
public record UpdateLocationVoteRequest(
        @Schema(description = "참여자 이름", example = "김철수")
        @NotBlank String participantName,

        @Schema(description = "출발지 주소", example = "서울시 마포구 합정동")
        @NotBlank String departureLocation,

        @Schema(description = "출발지 위도", example = "37.5495")
        @NotBlank String departureLat,

        @Schema(description = "출발지 경도", example = "126.9139")
        @NotBlank String departureLng
) {
}
