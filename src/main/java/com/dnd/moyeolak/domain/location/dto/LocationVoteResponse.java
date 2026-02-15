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

    @Schema(description = "출발지 주소", example = "수원시 장안구 영화동")
    String departureLocation,

    @Schema(description = "출발지 위도", example = "37.2994")
    BigDecimal departureLat,

    @Schema(description = "출발지 경도", example = "127.0085")
    BigDecimal departureLng
) {

    public static LocationVoteResponse from(LocationVote locationVote) {
        return new LocationVoteResponse(
            locationVote.getId(),
            locationVote.getDepartureName(),
            locationVote.getDepartureLocation(),
            locationVote.getDepartureLat(),
            locationVote.getDepartureLng()
        );
    }

}
