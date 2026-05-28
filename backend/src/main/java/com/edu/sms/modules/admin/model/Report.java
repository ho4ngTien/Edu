package com.edu.sms.modules.admin.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String type; // FINANCIAL, ACADEMIC, SYSTEM

    @Column(columnDefinition = "TEXT")
    private String data; // JSON string representing report data snapshot

    @CreationTimestamp
    private LocalDateTime generatedAt;

    private UUID generatedBy; // Soft Link -> Auth.auth_users.id
}
