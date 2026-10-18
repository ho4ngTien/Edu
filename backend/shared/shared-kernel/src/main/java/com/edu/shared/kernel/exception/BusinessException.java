package com.edu.shared.kernel.exception;

public class BusinessException extends RuntimeException {

    private final String code;
    private final int httpStatus;

    public BusinessException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public BusinessException(String code, String message) {
        this(code, message, 400);
    }

    public BusinessException(ErrorCode errorCode, String message, int httpStatus) {
        this(errorCode.name(), message, httpStatus);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.name(), message, 400);
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
