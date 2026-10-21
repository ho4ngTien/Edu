package com.edu.organization.domain.exception;

import com.edu.shared.kernel.exception.ResourceNotFoundException;

import java.util.UUID;

public class OrganizationNotFoundException extends ResourceNotFoundException {

    public OrganizationNotFoundException(UUID organizationId) {
        super("Organization", organizationId.toString());
    }
}
