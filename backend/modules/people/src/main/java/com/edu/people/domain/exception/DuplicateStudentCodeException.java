package com.edu.people.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateStudentCodeException extends BusinessException {

    public DuplicateStudentCodeException(String studentCode) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Student code '%s' already exists", studentCode),
                409
        );
    }
}

