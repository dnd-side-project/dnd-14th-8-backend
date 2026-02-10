package com.dnd.moyeolak.global.client.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GoogleDistanceMatrixResponse(
        @JsonProperty("origin_addresses") List<String> originAddresses,
        @JsonProperty("destination_addresses") List<String> destinationAddresses,
        List<Row> rows,
        String status
) {

    public record Row(List<Element> elements) {}

    public record Element(
            Distance distance,
            Duration duration,
            String status
    ) {}

    public record Distance(
            String text,
            int value   // meters
    ) {}

    public record Duration(
            String text,
            int value   // seconds
    ) {}
}
