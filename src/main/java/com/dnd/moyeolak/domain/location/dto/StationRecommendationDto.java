package com.dnd.moyeolak.domain.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "추천 지하철역 정보")
@Builder
public record StationRecommendationDto(
        @Schema(description = "추천 순위 (1=최적)", example = "1")
        int rank,

        @Schema(description = "역 ID", example = "321")
        Long stationId,

        @Schema(description = "지하철역 이름. 노선마다 이름이 다른 환승역은 합친 이름(\"총신대입구(이수)\")으로 내려준다", example = "디지털미디어시티역")
        String stationName,

        @Schema(description = "역 주소. 시도 이름을 줄이고 구까지만 내려준다. 주소가 없는 역은 null", example = "서울 은평구")
        String stationAddress,

        @Schema(
                description = "그 역의 노선 전체. 같은 역(이름이 같고 500m 안)의 노선을 1호선부터 9호선까지 번호순, "
                        + "그다음 가나다순, 영문으로 시작하는 노선은 마지막 순서로 담는다",
                example = "[\"6호선\", \"경의중앙선\", \"공항철도\"]"
        )
        List<String> lines,

        @Schema(description = "역 위도", example = "37.5779")
        double latitude,

        @Schema(description = "역 경도", example = "126.8997")
        double longitude,

        @Schema(description = "중심점과의 거리 (미터)", example = "1200")
        int distanceFromCenter,

        @Schema(description = "평균 대중교통 이동시간 (분)", example = "92")
        double avgTransitDuration,

        @Schema(description = "각 참여자의 이동 경로 목록")
        List<RouteDto> routes
) {}
