package com.edu.curriculum.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateSubjectCodeException extends BusinessException {

    public DuplicateSubjectCodeException(String code) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Subject code '%s' already exists in this organization", code),
                409
        );
    }
}

