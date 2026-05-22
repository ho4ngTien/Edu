package com.edu.sms.modules.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Soft Link to Auth Module
    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false)
    private String fullName;

    private String avatarUrl;

    private String phone;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
