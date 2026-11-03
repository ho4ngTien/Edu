package com.edu.curriculum.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

import java.util.UUID;

public class DeprecatedSubjectException extends BusinessException {

    public DeprecatedSubjectException(UUID subjectId) {
        super(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                String.format("Cannot assign DEPRECATED subject '%s' to curriculum", subjectId),
                422
        );
    }
}

