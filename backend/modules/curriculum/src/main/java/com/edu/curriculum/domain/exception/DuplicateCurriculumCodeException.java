package com.edu.curriculum.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateCurriculumCodeException extends BusinessException {

    public DuplicateCurriculumCodeException(String code) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Curriculum code '%s' already exists in this organization", code),
                409
        );
    }
}

