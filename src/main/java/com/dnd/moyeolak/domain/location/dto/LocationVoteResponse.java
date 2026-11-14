package com.dnd.moyeolak.domain.location.dto;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "출발지 투표 응답")
public record LocationVoteResponse(
    @Schema(description = "출발지 투표 ID", example = "1")
    Long locationVoteId,

    @Schema(description = "참여자 이름", example = "김철수")
    String participantName,

    @Schema(description = "출발지 이름 (검색으로 장소를 고르면 장소 이름, 현 위치나 주소로 고르면 주소)", example = "서대문역 5호선")
    String departureLocation,

    @Schema(description = "출발지로 고른 장소의 주소 (장소 이름으로 고른 경우에만 값이 있고, 그 밖에는 null)", example = "서울 종로구 통일로 지하 126")
    String departureAddress,

    @Schema(description = "출발지 위도", example = "37.5657519")
    BigDecimal departureLat,

    @Schema(description = "출발지 경도", example = "126.9666376")
    BigDecimal departureLng
) {

    public static LocationVoteResponse from(LocationVote locationVote) {
        return new LocationVoteResponse(
            locationVote.getId(),
            locationVote.getDepartureName(),
            locationVote.getDepartureLocation(),
            locationVote.getDepartureAddress(),
            locationVote.getDepartureLat(),
            locationVote.getDepartureLng()
        );
    }

}
