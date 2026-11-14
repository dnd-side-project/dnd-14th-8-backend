package com.dnd.moyeolak.domain.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "출발지 수정 요청")
public record UpdateLocationVoteRequest(
        @Schema(description = "참여자 이름", example = "김철수")
        @NotBlank String participantName,

        @Schema(description = "출발지 이름 (검색으로 장소를 고르면 장소 이름, 현 위치나 주소로 고르면 주소)", example = "서대문역 5호선")
        @NotBlank String departureLocation,

        @Schema(description = "출발지로 고른 장소의 주소 (장소 이름으로 고른 경우에만 전달, 생략하면 null로 덮어씀)", example = "서울 종로구 통일로 지하 126")
        String departureAddress,

        @Schema(description = "출발지 위도", example = "37.5657519")
        @NotBlank String departureLat,

        @Schema(description = "출발지 경도", example = "126.9666376")
        @NotBlank String departureLng
) {
}
