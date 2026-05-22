package com.edu.sms.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "auth_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name; // e.g. ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "auth_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
