package com.edu.sms.modules.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(unique = true, nullable = false)
    private String studentCode;

    private LocalDate enrollmentDate;
}
