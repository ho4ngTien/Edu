package com.edu.sms.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "auth_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. EDIT_GRADE, CREATE_CLASS

    private String description;
}
