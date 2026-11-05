package com.edu.academic.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateClassCodeException extends BusinessException {

    public DuplicateClassCodeException(String code) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Class code '%s' already exists in this organization", code),
                409
        );
    }
}

