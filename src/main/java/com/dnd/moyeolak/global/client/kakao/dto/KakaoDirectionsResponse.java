package com.dnd.moyeolak.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoDirectionsResponse(
        List<Route> routes
) {

    public record Route(
            Summary summary
    ) {}

    public record Summary(
            int distance,
            int duration,
            Fare fare
    ) {}

    public record Fare(
            @JsonProperty("toll")
            Integer toll,
            @JsonProperty("taxi")
            Integer taxi
    ) {
        public int safeToll() {
            return toll != null ? toll : 0;
        }

        public int safeTaxi() {
            return taxi != null ? taxi : 0;
        }
    }
}
