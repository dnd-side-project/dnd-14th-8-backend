package com.dnd.moyeolak.test.janghh.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "최적 만남 장소 요청")
public record OptimalLocationRequest(
    @ArraySchema(
        schema = @Schema(implementation = ParticipantInfo.class),
        arraySchema = @Schema(
            description = "참여 가능한 인원의 위치 목록 (최소 2명, 최대 10명 권장)",
            example = "[{\"name\":\"참여자1\",\"latitude\":37.5621,\"longitude\":126.8015},{\"name\":\"참여자2\",\"latitude\":37.4980,\"longitude\":127.0276},{\"name\":\"참여자3\",\"latitude\":37.5663,\"longitude\":126.9779}]"
        )
    )
    @NotEmpty(message = "참가자 정보는 필수입니다.")
    List<ParticipantInfo> participants
) {
    @Schema(description = "참가자 위치 정보")
    public record ParticipantInfo(
        @Schema(description = "참여자 이름", example = "참여자1")
        @NotNull(message = "참가자 이름은 필수입니다.")
        String name,

        @Schema(description = "참여자 위도 (WGS84)", example = "37.5621")
        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @Schema(description = "참여자 경도 (WGS84)", example = "126.8015")
        @NotNull(message = "경도는 필수입니다.")
        Double longitude
    ) {}
}
