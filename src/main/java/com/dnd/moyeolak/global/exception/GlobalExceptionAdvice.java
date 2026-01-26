package com.dnd.moyeolak.global.exception;

import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 다른 예외 처리기보다 우선순위 높게 설정
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /** 비즈니스 예외 처리 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException 발생: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unexpected exception 발생", ex);
        return ResponseEntity
                .status(ErrorCode.SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.SERVER_ERROR));
    }
}
