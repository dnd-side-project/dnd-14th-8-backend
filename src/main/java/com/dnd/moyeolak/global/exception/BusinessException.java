package com.dnd.moyeolak.global.exception;

import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
      super(errorCode.getMessage());
      this.errorCode = errorCode;
    }

}
