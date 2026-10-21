package com.edu.identity.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateUsernameException extends BusinessException {

    public DuplicateUsernameException(String username) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Username '%s' is already taken", username),
                409
        );
    }
}
