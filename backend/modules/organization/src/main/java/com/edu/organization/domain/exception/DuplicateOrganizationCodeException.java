package com.edu.organization.domain.exception;

import com.edu.shared.kernel.exception.BusinessException;
import com.edu.shared.kernel.exception.ErrorCode;

public class DuplicateOrganizationCodeException extends BusinessException {

    public DuplicateOrganizationCodeException(String code) {
        super(
                ErrorCode.DUPLICATE_RESOURCE,
                String.format("Organization with code '%s' already exists", code),
                409
        );
    }
}
