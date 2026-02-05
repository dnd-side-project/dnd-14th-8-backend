package com.dnd.moyeolak.test.janghh.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "평가된 추천 장소 정보")
public record EvaluatedPlace(
    @Schema(description = "추천 순위 (1=최적)", example = "1")
    int rank,

    @Schema(description = "장소명", example = "마포역 5호선")
    String name,

    @Schema(description = "장소 카테고리", example = "지하철역")
    String category,

    @Schema(description = "주소", example = "서울 마포구 도화동 160")
    String address,

    @Schema(description = "장소 위도", example = "37.53955402908912")
    double latitude,

    @Schema(description = "장소 경도", example = "126.94586257221307")
    double longitude,

    @Schema(description = "중심점과의 거리 (미터)", example = "944")
    int distanceFromCenter,

    @Schema(description = "총 이동시간 합 (분)", example = "82")
    int minSum,

    @Schema(description = "최대 이동시간 (분)", example = "35")
    int minMax,

    @Schema(description = "평균 이동시간 (분)", example = "27.33")
    double avgDuration,

    @ArraySchema(
        schema = @Schema(implementation = RouteDetail.class),
        arraySchema = @Schema(description = "각 참여자의 이동 경로 결과")
    )
    List<RouteDetail> routes
) {}
