package com.edu.organization.infrastructure.web;

import com.edu.organization.api.dto.OrganizationView;
import com.edu.organization.application.command.CreateOrganizationCommand;
import com.edu.organization.application.command.UpdateOrganizationCommand;
import com.edu.organization.application.service.OrganizationService;
import com.edu.organization.domain.model.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private OrganizationService organizationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrganizationController(organizationService))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/organizations creates organization and returns 201")
    void createOrganizationSuccess() throws Exception {
        UUID orgId = UUID.randomUUID();
        OrganizationView view = new OrganizationView(orgId, "HUST", "Hanoi University of Science and Technology", OrganizationStatus.ACTIVE, Instant.now());

        given(organizationService.createOrganization(any(CreateOrganizationCommand.class))).willReturn(view);

        String json = """
                {
                    "code": "HUST",
                    "name": "Hanoi University of Science and Technology"
                }
                """;

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.code").value("HUST"))
                .andExpect(jsonPath("$.name").value("Hanoi University of Science and Technology"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations with invalid body returns 400")
    void createOrganizationInvalidBody() throws Exception {
        String json = """
                {
                    "code": "",
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{id} returns 200 with OrganizationView")
    void getOrganizationById() throws Exception {
        UUID orgId = UUID.randomUUID();
        OrganizationView view = new OrganizationView(orgId, "HUST", "HUST Name", OrganizationStatus.ACTIVE, Instant.now());

        given(organizationService.getOrganization(orgId)).willReturn(view);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.code").value("HUST"));
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{id} updates organization and returns 200")
    void updateOrganization() throws Exception {
        UUID orgId = UUID.randomUUID();
        OrganizationView view = new OrganizationView(orgId, "HUST", "Updated Name", OrganizationStatus.ACTIVE, Instant.now());

        given(organizationService.updateOrganization(eq(orgId), any(UpdateOrganizationCommand.class))).willReturn(view);

        String json = """
                {
                    "name": "Updated Name"
                }
                """;

        mockMvc.perform(put("/api/v1/organizations/{organizationId}", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }
}
