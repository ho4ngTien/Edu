package com.edu.organization.application.port;

import java.util.UUID;

public interface OrganizationAccountQuery {

    boolean accountExists(UUID userId);

    boolean accountCanJoinOrganization(UUID userId);
}
