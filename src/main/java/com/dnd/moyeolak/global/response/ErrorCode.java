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

    // 클라이언트 오류
    UNAUTHORIZED("E401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("E404", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE("E409", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
    MEETING_NOT_FOUND("E410", "모임이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_POLL_NOT_FOUND("E411", "일정 투표판이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    LOCATION_POLL_NOT_FOUND("E412", "위치 투표판이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_LOCAL_STORAGE_KEY("E413", "이미 참여한 사용자입니다.", HttpStatus.CONFLICT),

    // 서버 오류
    SERVER_ERROR("E500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
