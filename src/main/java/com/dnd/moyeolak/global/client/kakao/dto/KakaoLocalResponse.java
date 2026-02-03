package com.dnd.moyeolak.global.client.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoLocalResponse(
    @JsonProperty("documents")
    List<KakaoDocument> documents,

    @JsonProperty("meta")
    Meta meta
) {
    public record Meta(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("pageable_count")
        int pageableCount,

        @JsonProperty("is_end")
        boolean isEnd
    ) {}
}
