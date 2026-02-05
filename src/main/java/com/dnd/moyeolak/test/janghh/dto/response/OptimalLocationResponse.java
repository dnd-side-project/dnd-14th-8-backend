package com.dnd.moyeolak.test.janghh.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "최적 만남 장소 추천 결과")
public record OptimalLocationResponse(
    @Schema(description = "모든 참가자의 중간 지점")
    CenterPoint centerPoint,

    @ArraySchema(
        schema = @Schema(implementation = EvaluatedPlace.class),
        arraySchema = @Schema(description = "추천 장소 목록 (순위 오름차순, 최대 3개)")
    )
    List<EvaluatedPlace> recommendations
) {}
