package com.edu.sms.modules.admin.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_system_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    private String id; // e.g. MAX_STUDENTS_PER_CLASS

    @Column(nullable = false)
    private String value;

    private String description;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private UUID updatedBy; // Soft Link -> Auth.auth_users.id
}
