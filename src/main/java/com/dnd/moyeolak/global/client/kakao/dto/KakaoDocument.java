package com.dnd.moyeolak.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoDocument(
    @JsonProperty("place_name")
    String placeName,

    @JsonProperty("category_name")
    String categoryName,

    @JsonProperty("category_group_code")
    String categoryGroupCode,

    @JsonProperty("category_group_name")
    String categoryGroupName,

    @JsonProperty("phone")
    String phone,

    @JsonProperty("address_name")
    String addressName,

    @JsonProperty("road_address_name")
    String roadAddressName,

    @JsonProperty("x")
    String x,

    @JsonProperty("y")
    String y,

    @JsonProperty("place_url")
    String placeUrl,

    @JsonProperty("distance")
    String distance
) {}
