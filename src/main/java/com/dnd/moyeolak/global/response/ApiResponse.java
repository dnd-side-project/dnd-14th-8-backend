package com.dnd.moyeolak.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 제외
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private String code;            // 비즈니스 코드
    private String message;         // 사용자 메시지
    private T data;                 // 응답 데이터

    public static ApiResponse<Void> success() {
        return success(SuccessCode.OK, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(SuccessCode.OK, data);
    }

    public static <T> ApiResponse<T> success(SuccessCode sc, T data) {
        return ApiResponse.<T>builder()
                .code(sc.getCode())
                .message(sc.getMessage())
                .data(data)
                .build();
    }

    public static ApiResponse<Void> error(ErrorCode ec) {
        return ApiResponse.<Void>builder()
                .code(ec.getCode())
                .message(ec.getMessage())
                .build();
    }

    // 상세 정보 포함 에러 (필요하다면 사용)
    public static <T> ApiResponse<T> error(ErrorCode ec, T data) {
        return ApiResponse.<T>builder()
                .code(ec.getCode())
                .message(ec.getMessage())
                .data(data)
                .build();
    }
}