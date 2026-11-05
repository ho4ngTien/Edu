package com.edu.people.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateTeacherCodeException extends BusinessException {

    public DuplicateTeacherCodeException(String teacherCode) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Teacher code '%s' already exists", teacherCode),
                409
        );
    }
}

