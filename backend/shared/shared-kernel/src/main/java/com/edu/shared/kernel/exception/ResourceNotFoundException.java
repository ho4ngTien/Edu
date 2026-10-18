package com.edu.shared.kernel.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND.name(),
                String.format("%s not found with identifier: %s", resourceName, identifier),
                404
        );
    }
}
