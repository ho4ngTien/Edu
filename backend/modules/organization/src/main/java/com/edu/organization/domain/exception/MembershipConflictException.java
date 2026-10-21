package com.edu.organization.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class MembershipConflictException extends BusinessException {

    public MembershipConflictException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message, 409);
    }
}
