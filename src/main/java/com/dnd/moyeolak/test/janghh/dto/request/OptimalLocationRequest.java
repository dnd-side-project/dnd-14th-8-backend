package com.dnd.moyeolak.test.janghh.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OptimalLocationRequest(
    @NotEmpty(message = "참가자 정보는 필수입니다.")
    List<ParticipantInfo> participants
) {
    public record ParticipantInfo(
        @NotNull(message = "참가자 이름은 필수입니다.")
        String name,

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude
    ) {}
}
