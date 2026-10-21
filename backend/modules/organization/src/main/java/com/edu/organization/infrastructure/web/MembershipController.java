package com.edu.organization.infrastructure.web;

import com.edu.organization.api.dto.InviteMemberRequest;
import com.edu.organization.api.dto.MembershipView;
import com.edu.organization.api.dto.UpdateMemberStatusRequest;
import com.edu.organization.application.command.InviteMemberCommand;
import com.edu.organization.application.command.UpdateMemberStatusCommand;
import com.edu.organization.application.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/members")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<MembershipView> invite(
            @PathVariable UUID organizationId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        InviteMemberCommand command = new InviteMemberCommand(
                organizationId,
                request.userId(),
                request.role()
        );
        MembershipView invited = membershipService.inviteMember(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(invited);
    }

    @GetMapping
    public ResponseEntity<List<MembershipView>> listMembers(@PathVariable UUID organizationId) {
        return ResponseEntity.ok(membershipService.listMembers(organizationId));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<MembershipView> updateStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberStatusRequest request
    ) {
        UpdateMemberStatusCommand command = new UpdateMemberStatusCommand(
                organizationId,
                userId,
                request.status()
        );
        return ResponseEntity.ok(membershipService.updateMemberStatus(command));
    }
}
