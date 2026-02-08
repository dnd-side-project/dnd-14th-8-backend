package com.dnd.moyeolak.domain.location.dto;

import com.dnd.moyeolak.domain.location.entity.LocationVote;

import java.math.BigDecimal;

public record LocationVoteResponse(
    Long locationVoteId,
    String participantName,
    String departureLocation,
    BigDecimal departureLat,
    BigDecimal departureLng
) {

    public static LocationVoteResponse from(LocationVote locationVote) {
        return new LocationVoteResponse(
            locationVote.getLocationVoteId(),
            locationVote.getDepartureName(),
            locationVote.getDepartureLocation(),
            locationVote.getDepartureLat(),
            locationVote.getDepartureLng()
        );
    }

}
