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
            String googleMapsUri,
            List<String> types,
            List<Photo> photos,
            RegularOpeningHours regularOpeningHours,
            List<Review> reviews,
            GenerativeSummary generativeSummary,
            ReviewSummary reviewSummary,
            EditorialSummary editorialSummary,
            Boolean parkingOptions,
            Boolean reservable,
            Boolean goodForGroups
    ) {}

    public record DisplayName(
            String text,
            String languageCode
    ) {}

    public record Location(
            Double latitude,
            Double longitude
    ) {}

    public record Photo(
            String name,
            Integer widthPx,
            Integer heightPx,
            List<AuthorAttribution> authorAttributions
    ) {
        public String toImageUrl(String apiKey, int maxWidthPx) {
            return "https://places.googleapis.com/v1/" + name + "/media?maxWidthPx=" + maxWidthPx + "&key=" + apiKey;
        }
    }

    public record AuthorAttribution(
            String displayName,
            String uri,
            String photoUri
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

    public record Review(
            String name,
            String relativePublishTimeDescription,
            Integer rating,
            Text text,
            Text originalText,
            AuthorAttribution authorAttribution,
            String publishTime
    ) {}

    public record Text(
            String text,
            String languageCode
    ) {}

    /** 장소에 대한 AI 생성 요약 */
    public record GenerativeSummary(
            Overview overview,
            String overviewFlagContentUri,
            DisclosureText disclosureText
    ) {}

    public record Overview(
            String text,
            String languageCode
    ) {}

    public record DisclosureText (
            String text,
            String languageCode
    ) {}

    /** 사용자 리뷰를 사용한 장소의 AI 생성 요약 */
    public record ReviewSummary(
        Text text,
        String flagContentUri,
        DisclosureText disclosureText,
        String reviewsUri
    ) {}


    /** 편집 요약(소개글) */
    public record EditorialSummary(
            String text,
            String languageCode
    ) {}
}
