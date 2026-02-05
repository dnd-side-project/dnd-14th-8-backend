package com.dnd.moyeolak.domain.participant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "위치 기반 참여자 생성 요청")
public record CreateParticipantWithLocationRequest(
        @Schema(description = "참여자 이름", example = "이영희")
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다")
        String name,

        @Schema(description = "브라우저 식별 키 (UUID 권장)", example = "550e8400-e29b-41d4-a716-446655440001")
        @NotBlank(message = "localStorageKey는 필수입니다")
        @Size(max = 100, message = "localStorageKey는 100자를 초과할 수 없습니다")
        String localStorageKey,

        @Schema(description = "출발지 위치 정보")
        @NotNull(message = "위치 정보는 필수입니다")
        @Valid
        LocationInput location
) {
    @Schema(description = "출발지 위치 정보")
    public record LocationInput(
            @Schema(description = "위도 (-90 ~ 90)", example = "37.5665")
            @NotNull(message = "위도는 필수입니다")
            @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다")
            @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다")
            BigDecimal latitude,

            @Schema(description = "경도 (-180 ~ 180)", example = "126.9780")
            @NotNull(message = "경도는 필수입니다")
            @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다")
            @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다")
            BigDecimal longitude,

            @Schema(description = "출발지 주소", example = "서울시 중구 명동")
            @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다")
            String address
    ) {
    }
}
