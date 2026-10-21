package com.edu.organization.infrastructure.web;

import com.edu.organization.api.dto.MembershipView;
import com.edu.organization.application.command.InviteMemberCommand;
import com.edu.organization.application.command.UpdateMemberStatusCommand;
import com.edu.organization.application.service.MembershipService;
import com.edu.organization.domain.model.MembershipStatus;
import com.edu.organization.domain.model.OrganizationRole;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MembershipControllerTest {

    @Mock
    private MembershipService membershipService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MembershipController(membershipService))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/organizations/{orgId}/members invites member and returns 201")
    void inviteMemberSuccess() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        MembershipView view = new MembershipView(
                membershipId, orgId, userId, OrganizationRole.MEMBER, MembershipStatus.INVITED, null
        );

        given(membershipService.inviteMember(any(InviteMemberCommand.class))).willReturn(view);

        String json = String.format("""
                {
                    "userId": "%s",
                    "role": "MEMBER"
                }
                """, userId);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/members", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(membershipId.toString()))
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.status").value("INVITED"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations/{orgId}/members returns 200 with list of members")
    void listMembers() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MembershipView view = new MembershipView(
                UUID.randomUUID(), orgId, userId, OrganizationRole.OWNER, MembershipStatus.ACTIVE, Instant.now()
        );

        given(membershipService.listMembers(orgId)).willReturn(List.of(view));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/members", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/v1/organizations/{orgId}/members/{userId}/status updates status and returns 200")
    void updateMemberStatus() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MembershipView view = new MembershipView(
                UUID.randomUUID(), orgId, userId, OrganizationRole.MEMBER, MembershipStatus.ACTIVE, Instant.now()
        );

        given(membershipService.updateMemberStatus(any(UpdateMemberStatusCommand.class))).willReturn(view);

        String json = """
                {
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/members/{userId}/status", orgId, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
