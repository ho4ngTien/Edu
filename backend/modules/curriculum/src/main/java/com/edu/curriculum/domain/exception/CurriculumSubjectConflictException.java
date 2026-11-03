package com.edu.curriculum.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

import java.util.UUID;

public class CurriculumSubjectConflictException extends BusinessException {

    public CurriculumSubjectConflictException(UUID curriculumId, UUID subjectId) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Subject '%s' is already assigned to curriculum '%s'", subjectId, curriculumId),
                409
        );
    }
}

