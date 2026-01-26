package com.dnd.moyeolak.global.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {
    // 공통 성공
    OK("S100", "요청이 성공적으로 처리되었습니다.", HttpStatus.OK),
    RESOURCE_CREATED("S101", "리소스가 성공적으로 생성되었습니다.", HttpStatus.CREATED)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
