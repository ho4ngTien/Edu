package com.edu.identity.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException(String email) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Email '%s' is already registered", email),
                409
        );
    }
}
