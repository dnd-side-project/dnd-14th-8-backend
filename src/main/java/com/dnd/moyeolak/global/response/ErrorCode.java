package com.dnd.moyeolak.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 요청 파라미터 / 검증 오류
    ERROR("E100", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("E101", "필수 입력 항목이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("E102", "입력 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER("E103", "유효성 검증 실패", HttpStatus.BAD_REQUEST),

    // 서버 오류
    SERVER_ERROR("E500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
