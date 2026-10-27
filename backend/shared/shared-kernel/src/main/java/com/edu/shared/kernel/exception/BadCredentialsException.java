package com.edu.shared.kernel.exception;

public class BadCredentialsException extends BusinessException {

    public BadCredentialsException(String message) {
        super(ErrorCode.BAD_CREDENTIALS, message, 401);
    }
}
