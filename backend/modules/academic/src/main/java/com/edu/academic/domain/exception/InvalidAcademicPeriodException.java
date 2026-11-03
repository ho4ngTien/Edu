package com.edu.academic.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class InvalidAcademicPeriodException extends BusinessException {

    public InvalidAcademicPeriodException(String message) {
        super(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                message,
                400
        );
    }
}

