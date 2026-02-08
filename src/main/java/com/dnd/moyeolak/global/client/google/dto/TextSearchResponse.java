package com.dnd.moyeolak.global.client.google.dto;

import java.util.List;

public record TextSearchResponse(
        List<Place> places
) {
    public record Place(
            String id,
            String formattedAddress,
            DisplayName displayName,
            Location location,
            List<String> types,
            RegularOpeningHours regularOpeningHours
    ) {}

    public record DisplayName(
            String text,
            String languageCode
    ) {}

    public record Location(
            Double latitude,
            Double longitude
    ) {}

    public record RegularOpeningHours(
            Boolean openNow,
            List<Period> periods,
            List<String> weekdayDescriptions
    ) {}

    public record Period(
            Open open,
            Close close
    ) {}

    public record Open(
            Integer day,
            Integer hour,
            Integer minute
    ) {}

    public record Close(
            Integer day,
            Integer hour,
            Integer minute
    ) {}

}
